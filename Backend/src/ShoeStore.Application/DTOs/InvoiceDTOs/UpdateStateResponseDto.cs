using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.InvoiceDTOs;

public sealed record UpdateStateAdminResponseDto(string OrderCode, InvoiceStatus Status, Guid PublicUserId);