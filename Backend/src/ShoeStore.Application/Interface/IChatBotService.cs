namespace ShoeStore.Application.Interface;

public interface IChatBotService
{
    // return IAsyncEnumerable for streaming content when AI answer is too long, so that the client can display the content in real time
    IAsyncEnumerable<string> GenerateCampaignAsync(CancellationToken token);
}