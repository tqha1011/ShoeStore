namespace ShoeStore.Application.DTOs.AuthDTOs;

public sealed record GooglePayloadDto(string Email,string? Name,string Subject,string? PictureUrl);