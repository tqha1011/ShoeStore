using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.Hub;

public interface INotifyBotResponse
{
    Task NotifyAddVariantDraftAsync(AddVariantResultDto result, Guid publicUserid);

    Task NotifyProductSearchResultAsync(SearchResultDto result, Guid publicUserid);
}