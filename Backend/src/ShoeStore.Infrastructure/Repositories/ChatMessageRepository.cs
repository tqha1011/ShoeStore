using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class ChatMessageRepository(AppDbContext context)
    : GenericRepository<ChatMessage, int>(context), IChatMessageRepository
{
    public async Task<List<ChatMessage>> GetHistoryChatMessageAsync(int sessionId, CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .Where(x => x.SessionId == sessionId)
            .OrderByDescending(c => c.CreatedAt)
            .Take(30)
            .ToListAsync(token);
    }
}