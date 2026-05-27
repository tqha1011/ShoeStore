namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IStoreAssistantPluginService
{
    Task<string> SearchInventory(string keyword, CancellationToken token);
}