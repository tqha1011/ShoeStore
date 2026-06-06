namespace ShoeStore.Application.DTOs.AuthDTOs;

public sealed record VerifyOtpCachedDto(string Email, string PasswordHash, string OtpCode);