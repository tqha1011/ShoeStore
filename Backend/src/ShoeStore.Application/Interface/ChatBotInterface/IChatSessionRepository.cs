using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IChatSessionRepository : IGenericRepository<ChatSession, int>
{
    IQueryable<ChatSession> GetChatSessionsByUserId(int userId);
}