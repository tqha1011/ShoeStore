using ErrorOr;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface.ChatBotInterface;

namespace ShoeStore.Application.Services;

public class ChatMessageService(
    IChatMessageRepository messageRepository,
    IChatSessionRepository chatSessionRepository) : IChatMessageService
{
    /// <summary>
    /// Get message in a session
    /// Apply cursor-pagination to load more messages when user scroll up in the chat window
    /// </summary>
    /// <param name="sessionPublicId"></param>
    /// <param name="cursor"></param>
    /// <param name="token"></param>
    /// <returns></returns>
    public async Task<ErrorOr<List<MessageResponseDto>>> GetMessagesInSessionAsync(Guid sessionPublicId,DateTime? cursor,
        CancellationToken token)
    {
        var sessionId = await chatSessionRepository.GetChatSessionIdByPublicIdAsync(sessionPublicId, token);
        if (sessionId == null) return Error.NotFound("ChatSession.NotFound", "Chat session not found.");
        
        return await messageRepository.GetMessagesInSessionAsync(sessionId.Value, cursor, token);
    }
}   