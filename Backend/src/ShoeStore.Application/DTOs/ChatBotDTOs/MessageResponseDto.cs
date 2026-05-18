using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record MessageResponseDto(Guid MessageId, string Content, ChatBotRole Role, DateTime CreatedAt);