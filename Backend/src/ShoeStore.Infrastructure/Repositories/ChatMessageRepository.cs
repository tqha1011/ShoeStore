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

    // Apply pagination
    // If cursor is null, get the latest messages. Otherwise, get messages before the cursor.
    // If message created_at is equal to cursor then compare the publicId
    public async Task<List<MessageResponseDto>> GetMessagesInSessionAsync(int sessionPublicId, DateTime? cursor,
        Guid? messageId,
        CancellationToken token)
    {
        var query = DbSet.AsQueryable().AsNoTracking();
        query = cursor == null
            ? query.Where(x => x.SessionId == sessionPublicId)
            : query.Where(x =>
                x.SessionId == sessionPublicId && (x.CreatedAt < cursor ||
                                                   (x.CreatedAt == cursor && x.PublicId < messageId)));

        var response = await query
            .OrderByDescending(c => c.CreatedAt)
            .ThenByDescending(c => c.PublicId)
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