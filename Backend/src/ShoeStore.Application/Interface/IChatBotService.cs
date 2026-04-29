using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface;

public interface IChatBotService
{
    Task<string> GenerateCampaignAsync(StatisticsDataDto data,CancellationToken token);
}