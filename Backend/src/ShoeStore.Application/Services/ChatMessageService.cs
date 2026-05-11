using ErrorOr;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.UserInterface;

namespace ShoeStore.Application.Services;

public class ChatMessageService(
    IChatMessageRepository messageRepository,
    IChatSessionRepository chatSessionRepository,
    IUserRepository userRepository) : IChatMessageService
{
    /// <summary>
    /// Get message in a session
    /// Apply cursor-pagination to load more messages when user scroll up in the chat window
    /// </summary>
    /// <param name="sessionPublicId"></param>
    /// <param name="publicUserId"></param>
    /// <param name="cursor"></param>
    /// <param name="token"></param>
    /// <returns></returns>
    public async Task<ErrorOr<List<MessageResponseDto>>> GetMessagesInSessionAsync(Guid sessionPublicId,Guid publicUserId,DateTime? cursor,
        CancellationToken token)
    {
        var userId = await userRepository.GetUserIdByPublicIdAsync(publicUserId, token);
        if (userId == null) return Error.NotFound("User.NotFound", "User not found.");
        var sessionId = await chatSessionRepository.GetChatSessionIdByPublicIdAsync(sessionPublicId,userId.Value,token);
        if (sessionId == null) return Error.NotFound("ChatSession.NotFound", "Chat session not found.");
        
        return await messageRepository.GetMessagesInSessionAsync(sessionId.Value, cursor, token);
    }
}   