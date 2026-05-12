namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record ChatMessageResponseDto(List<MessageResponseDto> Messages, string? NextCursor);