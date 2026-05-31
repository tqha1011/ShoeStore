using ErrorOr;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using ShoeStore.Application.DTOs.CheckOutDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.CheckoutInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Application.Utilities;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services;

public class CheckOutService(
    IProductVariantRepository productVariantRepository,
    IUnitOfWork unitOfWork,
    ICartItemRepository cartItemRepository,
    IInvoiceRepository invoiceRepository,
    IUserRepository userRepository,
    IVoucherRepository voucherRepository,
    IConfiguration configuration) : ICheckOutService
{
    public async Task<ErrorOr<CheckOutResponseDto>> PrepareCheckOutAsync(List<CheckOutRequestDto> checkOutList,
        Guid publicUserId, List<int> voucherIds, CancellationToken token)
    {
        // get variant list by variant id in check out list
        var variantIdList = checkOutList.Select(x => x.VariantId)
            .Distinct()
            .ToList();
        var variantsList = await productVariantRepository.GetListVariantsAsync(variantIdList, token);

        if (variantsList.Count < variantIdList.Count)
            return Error.NotFound("Variant.NotFound", "One or more variants are deleted.");

        var userAddress = await userRepository.GetUserDefaultAddressAsync(publicUserId, token);
        var shippingFee = userAddress.CalculateShip(); // 40k VND for user without address

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

        var totalProductPrice = items.Sum(x => x.SubTotal);
        if (voucherIds.Count > 0)
        {
            var vouchers = await voucherRepository.GetVouchersByIdsAsync(voucherIds, token);
            var validVouchers = new List<Voucher>();
            foreach (var voucherId in voucherIds)
            {
                var voucher = vouchers.GetValueOrDefault(voucherId);
                if (voucher == null || voucher.IsDeleted || voucher.ValidTo < DateTime.UtcNow ||
                    voucher.ValidFrom > DateTime.UtcNow)
                    return Error.Validation("Voucher.Invalid", $"Voucher {voucher?.VoucherName} is invalid.");
                if (voucher.MinOrderPrice > totalProductPrice)
                    return Error.Validation("Voucher.MinOrderPriceNotMet",
                        $"The order price does not meet the minimum requirement for voucher {voucher.VoucherName}.");
                validVouchers.Add(voucher);
            }

            totalProductPrice = CalculateFinalProductPrice(validVouchers!, null!, totalProductPrice);
            shippingFee = CalculateFinalShippingFee(validVouchers!, null!, shippingFee);
        }
        var finalPrice = totalProductPrice + shippingFee;

        var summary = new CheckOutSummaryDto(totalProductPrice, finalPrice, shippingFee);

        var warnings = items.Where(x => x.IsOutOfStock)
            .Select(x => $"{x.ProductName} is only have {x.StockAvailable} items.")
            .ToList();
        var response = new CheckOutResponseDto(items, summary, warnings);
        return response;
    }

    public async Task<ErrorOr<InvoiceDto>> PlaceOrderAsync(PlaceOrderRequestDto placeOrderRequestDto, Guid publicUserId,
        bool fromCart, CancellationToken token)
    {
        return await unitOfWork.ExecuteWithStrategyAsync<ErrorOr<InvoiceDto>>(async () =>
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
                    cartItemRepository.DeleteListCartItem(userCartItem);
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
                if (placeOrderRequestDto.PaymentId == 1)
                {
                    var shopBankCode = configuration["ShopBank:BankCode"];
                    var shopBankAccount = configuration["ShopBank:BankAccount"];
                    var shopBankName = configuration["ShopBank:BankName"];
                    return invoice.MapToInvoiceDto(shopBankCode, shopBankAccount, shopBankName);
                }
                return invoice.MapToInvoiceDto();
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
        var subTotal = variantsList.Select(variant =>
        {
            var quantity = variantQuantityById.GetValueOrDefault(variant.PublicId, 0);
            return variant.Price * quantity;
        }).Sum();

        var shippingFee = placeOrderRequestDto.Address.CalculateShip();

        var invoice = new Invoice
        {
            UserId = userId,
            OrderCode = GenerateOrderCode.Generate("DH"),
            FullName = placeOrderRequestDto.FullName,
            Phone = placeOrderRequestDto.PhoneNumber,
            ShippingAddress = placeOrderRequestDto.Address,
            Status = InvoiceStatus.Pending,
            PaymentId = placeOrderRequestDto.PaymentId,
            CreatedAt = DateTime.UtcNow,
            FinalPrice = subTotal,
            ShippingFee = shippingFee,
            InvoiceDetails = new List<InvoiceDetail>(),
            VoucherDetails = new List<VoucherDetail>()
        };

        foreach (var items in variantsList)
        {
            var quantity = variantQuantityById.GetValueOrDefault(items.PublicId, 0);
            var invoiceDetails = new InvoiceDetail
            {
                ProductVariantId = items.Id,
                Quantity = quantity,
                UnitPrice = items.Price
            };
            invoice.InvoiceDetails.Add(invoiceDetails);
        }

        // validate vouchers
        var validVouchers = ValidateVouchers(vouchersApplied, subTotal);

        // calculate the final price after applying voucher and shipping fee
        var priceAfterApplyVoucherProduct = CalculateFinalProductPrice(validVouchers, invoice, subTotal,false);
        var finalShippingFee = CalculateFinalShippingFee(validVouchers, invoice, shippingFee,false);
        invoice.FinalPrice = priceAfterApplyVoucherProduct + finalShippingFee;
        invoice.ShippingFee = finalShippingFee;
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

    private static List<Voucher?> ValidateVouchers(List<Voucher?> vouchers, decimal subTotal)
    {
        return vouchers.Where(v =>
                v != null && subTotal >= v.MinOrderPrice && v.ValidTo >= DateTime.UtcNow &&
                v.ValidFrom <= DateTime.UtcNow && !v.IsDeleted)
            .ToList();
    }

    private static decimal CalculateFinalProductPrice(List<Voucher?> validVouchers, Invoice invoice, decimal subTotal, bool isPrepare = true)
    {
        var currentTotal = subTotal;
        if (validVouchers.Count == 0) return currentTotal;
        foreach (var voucher in validVouchers)
            switch (voucher)
            {
                case null:
                    continue;
                case { DiscountType: DiscountType.FixedAmount, VoucherScope: VoucherScope.Product }:
                {
                    var discountAmountForThisVoucher = Math.Min(voucher.Discount, currentTotal);
                    currentTotal -= discountAmountForThisVoucher;

                    if (!isPrepare)
                    {
                        var voucherDetails = new VoucherDetail
                        {
                            InvoiceId = invoice.Id,
                            VoucherId = voucher.Id,
                            MoneyDiscount = discountAmountForThisVoucher
                        };
                        invoice.VoucherDetails.Add(voucherDetails);
                    }
                    break;
                }
                case { DiscountType: DiscountType.Percentage, VoucherScope: VoucherScope.Product }:
                {
                    var discountAmountForThisVoucher = currentTotal * voucher.Discount;

                    if (discountAmountForThisVoucher > voucher.MaxPriceDiscount)
                        discountAmountForThisVoucher = voucher.MaxPriceDiscount;
                    currentTotal -= discountAmountForThisVoucher;
                    if (!isPrepare)
                    {
                        var voucherDetails = new VoucherDetail
                        {
                            InvoiceId = invoice.Id,
                            VoucherId = voucher.Id,
                            MoneyDiscount = discountAmountForThisVoucher
                        };
                        invoice.VoucherDetails.Add(voucherDetails);
                    }
                    break;
                }
            }

        return currentTotal;
    }

    private static decimal CalculateFinalShippingFee(List<Voucher?> validVouchers, Invoice invoice, decimal shippingFee,bool isPrepare = true)
    {
        if (validVouchers.Count == 0) return shippingFee;
        foreach (var voucher in validVouchers)
            switch (voucher)
            {
                case null:
                    continue;
                case { DiscountType: DiscountType.FixedAmount, VoucherScope: VoucherScope.Shipping }:
                {
                    var discountAmountForThisVoucher = Math.Min(voucher.Discount, shippingFee);
                    shippingFee -= discountAmountForThisVoucher;
                    if (!isPrepare)
                    {
                        var voucherDetails = new VoucherDetail
                        {
                            InvoiceId = invoice.Id,
                            VoucherId = voucher.Id,
                            MoneyDiscount = discountAmountForThisVoucher
                        };
                        invoice.VoucherDetails.Add(voucherDetails);
                    }
                    break;
                }
                case { DiscountType: DiscountType.Percentage, VoucherScope: VoucherScope.Shipping }:
                {
                    var discountAmountForThisVoucher = shippingFee * voucher.Discount;

                    if (discountAmountForThisVoucher > voucher.MaxPriceDiscount)
                        discountAmountForThisVoucher = voucher.MaxPriceDiscount;
                    shippingFee -= discountAmountForThisVoucher;
                    if (!isPrepare)
                    {
                        var voucherDetails = new VoucherDetail
                        {
                            InvoiceId = invoice.Id,
                            VoucherId = voucher.Id,
                            MoneyDiscount = discountAmountForThisVoucher
                        };
                        invoice.VoucherDetails.Add(voucherDetails);
                    }
                    break;
                }
            }

        return shippingFee;
    }
}