using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.CheckOutDTOs;

public sealed record InvoiceDto(
    Guid InvoicePublicId,
    string OrderCode,
    string FullName,
    string ShippingAddress,
    string PhoneNumber,
    InvoiceStatus Status,
    decimal ShippingFee,
    decimal FinalPrice,
    DateTime CreatedAt,
    List<VoucherDto> Vouchers,
    List<InvoiceDetailDto> Details
);

public sealed record VoucherDto(
    Guid VoucherPublicId,
    string VoucherName,
    string? VoucherDescription,
    decimal Discount,
    VoucherScope VoucherScope,
    DiscountType DiscountType,
    decimal MaxPriceDiscount,
    decimal MinOrderPrice);

public sealed record InvoiceDetailDto(
    Guid InvoiceDetailPublicId,
    int InvoiceId,
    int ProductVariantId,
    int Quantity,
    decimal UnitPrice);