using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record ProductChatHistoryMessageDto(string Content, ChatBotRole Role);
