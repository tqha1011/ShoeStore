namespace ShoeStore.Application.DTOs.VoucherDTOs;

public sealed record VoucherTargetUserDto(
    int UserId,
    string Email,
    string Username);