namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record CreateCampaignRequestDto(Guid PublicSessionId, string Content);