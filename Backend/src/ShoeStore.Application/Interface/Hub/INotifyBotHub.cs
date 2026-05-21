using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.Hub;

public interface INotifyBotHub
{
    Task NotifyAddVariantResponse(AddVariantResultDto result);
}