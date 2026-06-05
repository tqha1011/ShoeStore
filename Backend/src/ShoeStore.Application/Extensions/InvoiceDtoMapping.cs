using ShoeStore.Application.DTOs.CheckOutDTOs;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Extensions;

public static class InvoiceDtoMapping
{
    public static InvoiceDto MapToInvoiceDto(this Invoice invoice, string? shopBankCode = null,
        string? shopBankAccount = null, string? shopAccountName = null)
    {
        return new InvoiceDto(
            invoice.PublicId,
            invoice.OrderCode,
            invoice.FullName,
            invoice.ShippingAddress,
            invoice.Phone,
            invoice.Status,
            invoice.ShippingFee,
            invoice.FinalPrice,
            invoice.CreatedAt,
            invoice.PaymentId,
            shopBankCode,
            shopBankAccount,
            shopAccountName
        );
    }
}