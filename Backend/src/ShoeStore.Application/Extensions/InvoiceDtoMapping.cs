using ShoeStore.Application.DTOs;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Extensions;

public static class InvoiceDtoMapping
{
    public static InvoiceDto MapToInvoiceDto(this Invoice invoice, List<Voucher?>? vouchers,
        List<InvoiceDetail> details)
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
            vouchers ?? [],
            details
        );
    }
}