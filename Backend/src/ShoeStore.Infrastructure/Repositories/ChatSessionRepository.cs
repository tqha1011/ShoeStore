using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class ChatSessionRepository(AppDbContext context)
    : GenericRepository<ChatSession, int>(context), IChatSessionRepository
{
    public IQueryable<ChatSession> GetChatSessionsByUserId(int userId)
    {
        return DbSet.Where(s => s.UserId == userId && s.IsActive);
    }
}