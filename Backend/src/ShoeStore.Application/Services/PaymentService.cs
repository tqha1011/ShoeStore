using System.Text.RegularExpressions;
using Microsoft.Extensions.Caching.Hybrid;
using ShoeStore.Application.Constants;
using ShoeStore.Application.DTOs.CheckOutDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.CheckoutInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services;

public class PaymentService(
    IInvoiceRepository invoiceRepository,
    IPaymentRepository paymentRepository,
    IPaymentTransactionRepository paymentTransactionRepository,
    IUnitOfWork unitOfWork,
    IUserVoucherRepository userVoucherRepository,
    HybridCache cache) : IPaymentService
{
    public async Task<bool> ProcessSepayWebhookAsync(SepayWebhookDto sepayWebhookDto, CancellationToken token)
    {
        if (currentUser.Id == null) return false;
        if (sepayWebhookDto.TransferType != "in") return true;

        var orderCode = sepayWebhookDto.Code ?? ExtractOrderCode(sepayWebhookDto.Content);
        if (string.IsNullOrEmpty(orderCode)) return false;

        var invoice = await invoiceRepository.GetInvoiceByOrderCodeAsync(orderCode, token);
        if (invoice == null) return false;
        var voucherId = invoice.VoucherDetails.Select(v => v.VoucherId).ToList();

        // prevent webhook call twice
        if (invoice.Status is InvoiceStatus.Paid or InvoiceStatus.Cancelled) return true;

        if (sepayWebhookDto.TransferAmount < invoice.FinalPrice) return false;
        var paymentMethodId = await paymentRepository.GetPaymentIdByCode("SEPAY", token);
        var paymentTransaction = new PaymentTransaction
        {
            RemoteTransactionId = sepayWebhookDto.Id.ToString(),
            OrderCode = orderCode,
            InvoiceId = invoice.Id,
            Amount = sepayWebhookDto.TransferAmount,
            Content = sepayWebhookDto.Content,
            CreatedAt = DateTime.UtcNow,
            PaymentId = paymentMethodId
        };
        paymentTransactionRepository.Add(paymentTransaction);
        invoice.PaymentTransactions.Add(paymentTransaction);
        invoice.Status = InvoiceStatus.Paid;
        var userVouchers = await userVoucherRepository.GetUserVouchersByIds(voucherId, invoice.UserId, token);
        foreach (var userVoucher in userVouchers)
        {
            userVoucher.ReservedCount -= 1;
            userVoucher.UsedAt = DateTime.UtcNow;
            userVoucher.UsedCount += 1;
            if (userVoucher.Voucher is { MaxUsagePerUser: > 0 } &&
                userVoucher.UsedCount >= userVoucher.Voucher.MaxUsagePerUser)
                userVoucher.IsUsed = true;
        }

        await unitOfWork.SaveChangesAsync(token);
        await cache.RemoveByTagAsync(CacheTag.Statistic, token);
        return true;
    }

    private static string? ExtractOrderCode(string content)
    {
        if (string.IsNullOrWhiteSpace(content)) return null;
        var normalizedContent = content.Replace(" ", "").ToUpper();
        var regex = new Regex(@"DH\d+");
        var match = regex.Match(normalizedContent);
        return match.Success ? match.Value : null;
    }
}