namespace ShoeStore.Application.DTOs.VoucherDTOs;

public sealed record VoucherNotificationDto(
    string Email,
    string UserName,
    int UserId,
    int VoucherId,
    string VoucherName,
    DateTime ValidTo);