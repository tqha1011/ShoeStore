using ErrorOr;
using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IChatBotService
{
    // return IAsyncEnumerable for streaming content when AI answer is too long, so that the client can display the content in real time
    Task<ErrorOr<IAsyncEnumerable<string>>> GenerateCampaignAsync(CreateCampaignRequestDto requestDto,
        CancellationToken token);

    Task<ErrorOr<IAsyncEnumerable<string>>> ChatAskAboutStatisticsAsync(Guid publicSessionId,
        ChatMessageRequestDto messageRequestDto, CancellationToken token);

    Task<ErrorOr<IAsyncEnumerable<string>>> ChatAskAboutProductsAsync(Guid publicSessionId,
        ChatMessageRequestDto messageRequestDto, CancellationToken token);
}