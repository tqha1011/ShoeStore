using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs;

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
    List<Voucher?> Vouchers,
    List<InvoiceDetail> Details
);