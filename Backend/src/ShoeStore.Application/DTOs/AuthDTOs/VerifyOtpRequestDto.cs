namespace ShoeStore.Application.DTOs.AuthDTOs;

public record VerifyOtpRequestDto(string Email, string OtpCode);