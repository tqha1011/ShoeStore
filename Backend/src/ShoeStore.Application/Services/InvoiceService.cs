using ErrorOr;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Caching.Hybrid;
using Microsoft.Extensions.Configuration;
using ShoeStore.Application.Constants;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.InvoiceDetailDTOs;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Application.Rules;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services;

public class InvoiceService(
    IInvoiceRepository invoiceRepository,
    IUnitOfWork uow,
    ICurrentUser currentUser,
    HybridCache cache,
    IConfiguration configuration,
    IUserVoucherRepository userVoucherRepository) : IInvoiceService
{
    public async Task<ErrorOr<PageResult<InvoiceResponseDto>>> GetInvoiceAsync(InvoiceRequestDto request,
        CancellationToken token)
    {
        var query = invoiceRepository.GetAll();

        // check admin or user
        if (!currentUser.IsAdmin)
            query = query.Where(i => i.User != null && i.User.PublicId == currentUser.Id);

        query = query.ApplyInvoiceFilters(request);

        var totalCount = await query.CountAsync(token);

        query = query.OrderByDescending(iv => iv.CreatedAt).ApplyPagination(request.PageNumber, request.PageSize);

        var shopBankCode = configuration["ShopBank:BankCode"] ??
                           throw new InvalidOperationException("Shop bank code is not configured");
        var shopBankAccount = configuration["ShopBank:BankAccount"] ??
                              throw new InvalidOperationException("Shop bank account is not configured");
        var shopAccountName = configuration["ShopBank:AccountName"] ??
                              throw new InvalidOperationException("Shop account name is not configured");

        var invoices = await query.Select(i => new InvoiceResponseDto
        {
            PublicId = i.PublicId,
            Username = i.FullName,
            DateCreated = i.CreatedAt,
            UpdateCreated = i.UpdatedAt,
            Status = i.Status,
            PaymentName = i.Payment != null ? i.Payment.Name : string.Empty,
            Address = i.ShippingAddress,
            Phone = i.Phone,
            OrderCode = i.OrderCode,
            ShippingFee = i.ShippingFee,
            FinalPrice = i.FinalPrice,
            ShopBankCode = shopBankCode,
            ShopBankAccount = shopBankAccount,
            ShopAccountName = shopAccountName
        }).ToListAsync(token);

        if (currentUser.IsAdmin)
            invoices = invoices.Where(i => i.Status != InvoiceStatus.Cancelled).ToList();

        var pageResult = new PageResult<InvoiceResponseDto>
        {
            Items = invoices,
            TotalCount = totalCount,
            PageNumber = request.PageNumber,
            PageSize = request.PageSize
        };

        return pageResult;
    }

    public async Task<ErrorOr<IEnumerable<InvoiceDetailResponseDto>>> GetInvoiceDetailAsync(Guid invoiceGuid,
        CancellationToken token)
    {
        var details = invoiceRepository.GetInvoiceDetail(invoiceGuid);

        var result = await details.Select(d => new InvoiceDetailResponseDto
        {
            ProductName = d.ProductVariant!.Product!.ProductName,
            Size = d.ProductVariant.Size!.Size,
            Color = d.ProductVariant.Color!.ColorName,
            Quantity = d.Quantity,
            UnitPrice = d.UnitPrice,
            ImageUrl = d.ProductVariant.ImageUrl ?? string.Empty
        }).ToListAsync(token);

        if (result.Count == 0) return Error.NotFound("InvoiceDetail.NotFound", "Invoice detail not found");
        return result;
    }

    public async Task<ErrorOr<string>> UpdateInvoiceStateByUserAsync(Guid invoiceGuid, UpdateStateRequestDto request,
        CancellationToken token)
    {
        var invoice = await invoiceRepository.GetByPublicIdAsync(invoiceGuid, token);
        if (invoice == null) return Error.NotFound("Invoice.NotFound", "Invoice not found");
        if (invoice.User?.PublicId != currentUser.Id)
            return Error.Unauthorized("User.Unauthorized", "You are not authorized to update this invoice");

        if (!UpdateInvoiceStateRule.CanClientUpdateState(invoice.Status, request.Status))
            return Error.Forbidden("Invoice.Forbidden", "Client cannot change to this status");

        if (request.Status == InvoiceStatus.Cancelled)
        {
            await ReleaseReservedVouchersAsync(invoice, token);
            RestoreInvoiceStocks(invoice);
        }

        invoice.Status = request.Status;
        invoice.UpdatedAt = DateTime.UtcNow;
        invoiceRepository.Update(invoice);
        await uow.SaveChangesAsync(token);
        return invoice.OrderCode;
    }

    public async Task<ErrorOr<UpdateStateAdminResponseDto>> UpdateInvoiceStateByAdminAsync(Guid invoiceGuid,
        UpdateStateRequestDto request,
        CancellationToken token)
    {
        var invoice = await invoiceRepository.GetByPublicIdAsync(invoiceGuid, token);
        if (invoice == null) return Error.NotFound("Invoice.NotFound", "Invoice not found");
        if (!UpdateInvoiceStateRule.CanAdminUpdateState(invoice.Status, request.Status, invoice.PaymentId))
            return Error.Forbidden("Invoice.Forbidden", "Admin cannot change to this status");

        // check if client had paid enough for the invoice before marking as paid, only apply for online payment (paymentId = 1)
        if (request.Status == InvoiceStatus.Paid && invoice.PaymentId == (int)PaymentMethod.SePay)
        {
            var invoicePaymentResult = CheckEnoughInvoicePayment(invoice.PaymentTransactions.ToList(),
                invoice.FinalPrice);
            if (invoicePaymentResult.IsError) return invoicePaymentResult.FirstError;
        }

        if (request.Status == InvoiceStatus.Paid && invoice.PaymentId == (int)PaymentMethod.Cod)
            await MarkReservedVouchersAsUsedAsync(invoice, token);

        if (request.Status == InvoiceStatus.Cancelled)
        {
            if (invoice.Status == InvoiceStatus.Pending)
                await ReleaseReservedVouchersAsync(invoice, token);
            RestoreInvoiceStocks(invoice);
        }

        invoice.Status = request.Status;
        invoice.UpdatedAt = DateTime.UtcNow;
        invoiceRepository.Update(invoice);
        await uow.SaveChangesAsync(token);
        if (request.Status == InvoiceStatus.Paid) await cache.RemoveByTagAsync(CacheTag.Statistic, token);
        return new UpdateStateAdminResponseDto(invoice.OrderCode, invoice.Status, invoice.User!.PublicId);
    }

    // Check the invoice status when confirm paid by Sepay
    public async Task<ErrorOr<InvoiceCheckResultDto>> CheckInvoicePaymentStatusAsync(string orderCode,
        CancellationToken token)
    {
        if (string.IsNullOrEmpty(orderCode))
            return Error.Validation("Invoice.InvalidOrderCode", "Order code cannot be null or empty");
        var invoice = await invoiceRepository.GetInvoiceByOrderCodeAsync(orderCode, token);
        if (invoice == null) return Error.NotFound("Invoice.NotFound", "Invoice not found");
        if (invoice.User?.PublicId != currentUser.Id)
            return Error.Unauthorized("User.Unauthorized", "You are not authorized to check this invoice");
        var paymentTransaction = invoice.PaymentTransactions.ToList();
        var amountPaid = paymentTransaction.Sum(pt => pt.Amount);
        var remainingAmount = Math.Max(0, invoice.FinalPrice - amountPaid);
        // if remaining = 0 => status = Paid
        // remaining > 0 and < final price => status = PartiallyPaid
        // remaining = final price => status = Pending
        var status = remainingAmount == 0
            ? InvoiceStatus.Paid
            : remainingAmount > 0 && remainingAmount < invoice.FinalPrice
                ? InvoiceStatus.PartiallyPaid
                : InvoiceStatus.Pending;
        return new InvoiceCheckResultDto(status, invoice.OrderCode, invoice.FinalPrice, amountPaid, remainingAmount);
    }

    private static ErrorOr<Success> CheckEnoughInvoicePayment(List<PaymentTransaction> paymentTransactions,
        decimal finalPrice)
    {
        if (paymentTransactions.Count == 0)
            return Error.Validation("Invoice.InvalidStatus",
                "Cannot mark as paid without payment transactions for online payment");
        var totalAmount = paymentTransactions.Sum(pt => pt.Amount);
        if (totalAmount >= finalPrice)
            return Result.Success;
        return Error.Validation("Invoice.InvalidStatus",
            "Total payment amount is not enough to mark the invoice as paid");
    }

    private async Task MarkReservedVouchersAsUsedAsync(Invoice invoice, CancellationToken token)
    {
        var voucherIds = invoice.VoucherDetails.Select(v => v.VoucherId).ToList();
        var userVouchers = await userVoucherRepository.GetUserVouchersByIds(voucherIds, invoice.UserId, token);
        foreach (var userVoucher in userVouchers)
        {
            userVoucher.ReservedCount = Math.Max(0, userVoucher.ReservedCount - 1);
            userVoucher.UsedAt = DateTime.UtcNow;
            userVoucher.UsedCount += 1;
            if (userVoucher.Voucher is { MaxUsagePerUser: > 0 } &&
                userVoucher.UsedCount >= userVoucher.Voucher.MaxUsagePerUser)
                userVoucher.IsUsed = true;
        }
    }

    private async Task ReleaseReservedVouchersAsync(Invoice invoice, CancellationToken token)
    {
        var voucherIds = invoice.VoucherDetails.Select(v => v.VoucherId).ToList();
        var userVouchers = await userVoucherRepository.GetUserVouchersByIds(voucherIds, invoice.UserId, token);
        foreach (var userVoucher in userVouchers)
            userVoucher.ReservedCount = Math.Max(0, userVoucher.ReservedCount - 1);
    }

    private static void RestoreInvoiceStocks(Invoice invoice)
    {
        foreach (var detail in invoice.InvoiceDetails)
            if (detail.ProductVariant != null)
                detail.ProductVariant.Stock += detail.Quantity;
    }
}
