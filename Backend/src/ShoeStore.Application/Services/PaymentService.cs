using System.Text.RegularExpressions;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services;

public class PaymentService(
    IInvoiceRepository invoiceRepository,
    IPaymentRepository paymentRepository,
    IPaymentTransactionRepository paymentTransactionRepository,
    IUnitOfWork unitOfWork) : IPaymentService
{
    public async Task<bool> ProcessSepayWebhookAsync(SepayWebhookDto sepayWebhookDto, CancellationToken token)
    {
        if (sepayWebhookDto.TransferType != "in") return true;

        var orderCode = sepayWebhookDto.Code ?? ExtractOrderCode(sepayWebhookDto.Content);
        if (string.IsNullOrEmpty(orderCode)) return false;

        var invoice = await invoiceRepository.GetInvoiceByOrderCodeAsync(orderCode, token);
        if (invoice == null) return false;

        // prevent webhook call twice
        if (invoice.Status == InvoiceStatus.Paid) return true;

        if (sepayWebhookDto.TransferAmount < invoice.FinalPrice) return false;
        var paymentMethodId = await paymentRepository.GetPaymentIdByCode("SEPAY", token);
        var paymenTransaction = new PaymentTransaction
        {
            RemoteTransactionId = sepayWebhookDto.Id.ToString(),
            OrderCode = orderCode,
            InvoiceId = invoice.Id,
            Amount = sepayWebhookDto.TransferAmount,
            Content = sepayWebhookDto.Content,
            CreatedAt = DateTime.UtcNow,
            PaymentId = paymentMethodId
        };
        paymentTransactionRepository.Add(paymenTransaction);
        await unitOfWork.SaveChangesAsync(token);
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