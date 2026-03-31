namespace ShoeStore.Application.DTOs.RestorePasswordDto;

public sealed record OtpVerifyDto(string Email, string Otp);