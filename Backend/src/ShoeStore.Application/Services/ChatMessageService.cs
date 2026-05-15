using ErrorOr;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.UserInterface;

namespace ShoeStore.Application.Services;

public class ChatMessageService(
    IChatMessageRepository messageRepository,
    IChatSessionRepository chatSessionRepository,
    IUserRepository userRepository) : IChatMessageService
{
    /// <summary>
    ///     Get message in a session
    ///     Apply cursor-pagination to load more messages when user scroll up in the chat window
    ///     Return next cursor for FE
    /// </summary>
    /// <param name="sessionPublicId"></param>
    /// <param name="publicUserId"></param>
    /// <param name="cursor"></param>
    /// <param name="token"></param>
    /// <returns></returns>
    public async Task<ErrorOr<ChatMessageResponseDto>> GetMessagesInSessionAsync(Guid sessionPublicId,
        Guid publicUserId, string? cursor,
        CancellationToken token)
    {
        var userId = await userRepository.GetUserIdByPublicIdAsync(publicUserId, token);
        if (userId == null) return Error.NotFound("User.NotFound", "User not found.");
        var sessionId =
            await chatSessionRepository.GetChatSessionIdByPublicIdAsync(sessionPublicId, userId.Value, token);
        if (sessionId == null) return Error.NotFound("ChatSession.NotFound", "Chat session not found.");

        var dataCursor = cursor.ConvertToCursor();
        var response = await messageRepository.GetMessagesInSessionAsync(sessionId.Value, dataCursor?.CreatedAt,
            dataCursor?.PublicId, token);
        var result = new ChatMessageResponseDto(response,
            response.Count != 0
                ? $"{response.LastOrDefault()?.CreatedAt:o}_{response.LastOrDefault()?.MessageId}".ConvertToBase64()
                : null);
        return result;
    }
}
