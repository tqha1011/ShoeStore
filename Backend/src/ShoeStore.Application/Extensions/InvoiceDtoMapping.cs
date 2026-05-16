using ShoeStore.Application.DTOs.CheckOutDTOs;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Extensions;

public static class InvoiceDtoMapping
{
    public static InvoiceDto MapToInvoiceDto(this Invoice invoice, List<Voucher?>? vouchers,
        List<InvoiceDetail> details)
    {
        var voucherDto = vouchers?.Select(v => new VoucherDto(
            v!.PublicId,
            v.VoucherName,
            v.VoucherDescription,
            v.Discount,
            v.VoucherScope,
            v.DiscountType,
            v.MaxPriceDiscount,
            v.MinOrderPrice)).ToList();

        var invoiceDetails = details.Select(d => new InvoiceDetailDto(
            d.PublicId,
            d.InvoiceId,
            d.ProductVariantId,
            d.Quantity,
            d.UnitPrice)).ToList();

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
            voucherDto ?? [],
            invoiceDetails
        );
    }
}