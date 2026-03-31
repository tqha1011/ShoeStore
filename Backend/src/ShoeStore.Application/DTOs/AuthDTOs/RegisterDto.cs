namespace ShoeStore.Application.DTOs.AuthDTOs;

public sealed record RegisterDto(string Email, string Password, string ConfirmPassword);