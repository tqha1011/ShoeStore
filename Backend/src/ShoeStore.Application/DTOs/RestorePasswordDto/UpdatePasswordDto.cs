namespace ShoeStore.Application.DTOs.RestorePasswordDto;

public sealed record UpdatePasswordDto(string Email, string Otp, string NewPassword);