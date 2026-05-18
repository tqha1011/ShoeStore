namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record UpdateTitleRequestDto(Guid PublicSessionId, string Content,int UserId ,bool IsGenerateCampaign = false);