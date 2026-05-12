using Microsoft.EntityFrameworkCore;
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

    public async Task<int?> GetChatSessionIdByPublicIdAsync(Guid publicSessionId,int userId ,CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .Where(s => s.PublicId == publicSessionId && s.IsActive && s.UserId == userId).Select(c => c.Id)
            .FirstOrDefaultAsync(token);
    }
}