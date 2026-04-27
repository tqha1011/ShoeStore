namespace ShoeStore.Application.DTOs.VoucherDTOs;

public sealed record VoucherNotificationDto(
    List<VoucherTargetUserDto> TargetUsers,
    int VoucherId,
    string VoucherName,
    DateTime? ValidTo);