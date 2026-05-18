namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record ChatSessionResponseDto(Guid PublicId, Guid PublicUserId, string? Title, DateTime CreatedAt);