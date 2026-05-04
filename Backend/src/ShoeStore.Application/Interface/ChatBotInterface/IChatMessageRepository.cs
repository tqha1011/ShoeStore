using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IChatMessageRepository : IGenericRepository<ChatMessage, int>
{
    Task<List<ChatMessage>> GetHistoryChatMessageAsync(int sessionId, CancellationToken token);
}