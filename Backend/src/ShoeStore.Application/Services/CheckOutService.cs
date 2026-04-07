using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services;

public class CheckOutService(
    IProductVariantRepository productVariantRepository,
    IUnitOfWork unitOfWork,
    ICartItemRepository cartItemRepository,
    IInvoiceRepository invoiceRepository,
    IUserRepository userRepository) : ICheckOutService
{
    public async Task<ErrorOr<CheckOutResponseDto>> PrepareCheckOutAsync(List<CheckOutRequestDto> checkOutList,
        CancellationToken token)
    {
        // get variant list by variant id in check out list
        var variantIdList = checkOutList.Select(x => x.VariantId)
            .Distinct()
            .ToList();
        var variantsList = await productVariantRepository.GetListVariantsAsync(variantIdList, token);

        if (variantsList.Count < variantIdList.Count)
            return Error.NotFound("Variant.NotFound", "One or more variants are deleted.");

        // transform a list to a dictionary to optimize performance
        // Use GroupBy to sum the quantity if it has duplicate variantId in check out list
        var variantQuantityById = ConvertToDictionary(checkOutList);

        var items = variantsList.Select(variant =>
            {
                var quantity = variantQuantityById.GetValueOrDefault(variant.PublicId, 0);
                var isOutOfStock = variant.Stock <= 0 || quantity > variant.Stock;
                var subTotal = variant.Price * quantity;

                return new CheckOutItemDto(
                    variant.PublicId,
                    variant.Product?.ProductName ?? string.Empty,
                    variant.Color?.ColorName ?? string.Empty,
                    variant.Size?.Size ?? 0,
                    variant.Price,
                    quantity,
                    variant.Stock,
                    isOutOfStock,
                    subTotal
                );
            })
            .ToList();

        var total = items.Sum(x => x.SubTotal);
        var summary = new CheckOutSummaryDto(total, total);

        var warnings = items.Where(x => x.IsOutOfStock)
            .Select(x => $"{x.ProductName} is only have {x.StockAvailable} items.")
            .ToList();
        var response = new CheckOutResponseDto(items, summary, warnings);
        return response;
    }

    public async Task<ErrorOr<Created>> PlaceOrderAsync(PlaceOrderRequestDto placeOrderRequestDto, Guid publicUserId,
        bool fromCart, CancellationToken token)
    {
        return await unitOfWork.ExecuteInTransactionAsync<ErrorOr<Created>>(async () =>
        {
            using var transaction = await unitOfWork.BeginTransactionAsync(token);
            // transaction has 4 stages
            // stage 1: check the variant, the vouchers user chose
            // stage 2: delete user's cartItem if the order is from cart
            // stage 3: decrease the variant's stocks
            // stage 4: create invoice and invoice details
            // if 4 stages execute successfully, commit the transaction, otherwise rollback the transaction
            try
            {
                // In stage 1, check if the variant is valid, if the vouchers user chose are valid and not used
                // if the user is valid
                var user = await userRepository.GetUserByPublicIdAsync(publicUserId, token);
                if (user == null) return Error.NotFound("User.NotFound", "Cannot find user.");
                var variantIdList = placeOrderRequestDto.Items.Select(x => x.VariantId).Distinct().ToList();
                var voucherIdList = placeOrderRequestDto.VoucherIds?.Distinct().ToList() ?? [];
                var variantsList = await productVariantRepository.GetListVariantsAsync(variantIdList, token);

                if (variantsList.Count < variantIdList.Count)
                    return Error.NotFound("Variant.NotFound", "One or more variants are deleted.");

                var variantQuantityById = ConvertToDictionary(placeOrderRequestDto.Items);

                var vouchersApplied = MarkUsedVoucher(voucherIdList, user);

                if (vouchersApplied.Count < voucherIdList.Count)
                    return Error.NotFound("Voucher.NotFound", "One or more vouchers are not found or already used.");

                // Stage 2: delete user's cartItem if the order is from cart
                if (fromCart)
                {
                    var userCartItem = user.CartItems.Where(cartItem =>
                            variantIdList.Contains(cartItem.ProductVariant?.PublicId ?? Guid.Empty))
                        .Distinct()
                        .ToList();
                    cartItemRepository.DeleteCartItem(userCartItem);
                }

                // Stage 3: decrease the variant's stocks
                // check if the stock is enough, if not, return error and rollback the transaction
                var deductResult = DeductStocks(variantsList, variantQuantityById);
                if (deductResult.IsError) return deductResult.Errors;

                // Stage 4: create invoice and invoice details
                var invoice = CreateInvoice(placeOrderRequestDto, user.Id, variantsList, variantQuantityById,
                    vouchersApplied);
                invoiceRepository.Add(invoice);

                // If all stages execute successfully, commit the transaction, otherwise rollback the transaction
                await unitOfWork.SaveChangesAsync(token);
                await unitOfWork.CommitTransactionAsync(token);
                return Result.Created;
            }
            catch (DbUpdateConcurrencyException)
            {
                await unitOfWork.RollbackTransactionAsync(token);
                return Error.Conflict("Checkout.Concurrency", "Your product had been sold. Please try again");
            }
            catch (Exception)
            {
                await unitOfWork.RollbackTransactionAsync(token);
                return Error.Unexpected("InvoiceCreation.Failed",
                    "An unexpected error occurred while creating the order.");
            }
        }, token);
    }

    private static Dictionary<Guid, int> ConvertToDictionary(List<CheckOutRequestDto> checkOutList)
    {
        var variantQuantityById = checkOutList.GroupBy(x => x.VariantId)
            .ToDictionary(x => x.Key, x => x.Sum(y => y.Quantity));
        return variantQuantityById;
    }

    private static decimal CalculateFinalPrice(List<ProductVariant> variantsList,
        Dictionary<Guid, int> variantQuantityById,
        List<Voucher?> vouchers)
    {
        var subTotal = variantsList.Select(variant =>
        {
            var quantity = variantQuantityById.GetValueOrDefault(variant.PublicId, 0);
            return variant.Price * quantity;
        }).Sum();

        return vouchers.Aggregate(subTotal, (current, voucher) => (1 - (voucher?.Discount ?? 0)) * current);
    }

    private static List<Voucher?> MarkUsedVoucher(List<int> voucherIds, User user)
    {
        if (voucherIds.Count == 0) return []; // return empty if user didn't choose any voucher

        var userVoucher = user.UserVouchers.Where(uv => voucherIds.Contains(uv.VoucherId) && !uv.IsUsed)
            .ToList();

        foreach (var items in userVoucher)
        {
            items.IsUsed = true;
            items.UsedAt = DateTime.UtcNow;
        }

        var vouchers = userVoucher.Select(uv => uv.Voucher).ToList();
        return vouchers;
    }

    private static Invoice CreateInvoice(PlaceOrderRequestDto placeOrderRequestDto, int userId,
        List<ProductVariant> variantsList,
        Dictionary<Guid, int> variantQuantityById, List<Voucher?> vouchersApplied)
    {
        var invoice = new Invoice
        {
            UserId = userId,
            FullName = placeOrderRequestDto.FullName,
            Phone = placeOrderRequestDto.PhoneNumber,
            ShippingAddress = placeOrderRequestDto.Address,
            Status = InvoiceStatus.Pending,
            PaymentId = placeOrderRequestDto.PaymentId,
            CreatedAt = DateTime.UtcNow,
            FinalPrice = CalculateFinalPrice(variantsList, variantQuantityById, vouchersApplied),
            InvoiceDetails = new List<InvoiceDetail>()
        };

        foreach (var items in variantsList)
        {
            var quantity = variantQuantityById.GetValueOrDefault(items.PublicId, 0);
            var invoiceDetails = new InvoiceDetail
            {
                ProductVariantId = items.Id,
                Quantity = quantity,
                UnitPrice = items.Price * quantity
            };
            invoice.InvoiceDetails.Add(invoiceDetails);
        }

        return invoice;
    }

    private static ErrorOr<Success> DeductStocks(List<ProductVariant> variantsList,
        Dictionary<Guid, int> variantQuantityById)
    {
        foreach (var items in variantsList)
        {
            var stockAvailable = items.Stock - variantQuantityById.GetValueOrDefault(items.PublicId, 0);
            if (stockAvailable < 0)
                return Error.Validation("Stock.NotEnough",
                    $"The stock of {items.Product?.ProductName} is not enough.");
            items.Stock = stockAvailable;
        }

        return Result.Success;
    }
}