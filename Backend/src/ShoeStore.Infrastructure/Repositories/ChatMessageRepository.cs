using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.ChatBotDTOs;
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

    public async Task<List<MessageResponseDto>> GetMessagesInSessionAsync(int sessionPublicId, DateTime? cursor,
        CancellationToken token)
    {
        var query = DbSet.AsQueryable().AsNoTracking();
        query = cursor == null
            ? query.Where(x => x.SessionId == sessionPublicId)
            : query.Where(x => x.SessionId == sessionPublicId && x.CreatedAt < cursor);

        var response = await query
            .OrderByDescending(c => c.CreatedAt)
            .Select(m => new MessageResponseDto(
                m.PublicId,
                m.Content,
                m.Role,
                m.CreatedAt))
            .Take(25)
            .ToListAsync(token);
        return response;
    }
}