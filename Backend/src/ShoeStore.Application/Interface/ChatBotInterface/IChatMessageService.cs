using ErrorOr;
using ShoeStore.Application.Constants;
using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IChatMessageService
{
    Task<ErrorOr<ChatMessageResponseDto>> GetMessagesInSessionAsync(Guid sessionPublicId,Guid publicUserId,string? cursor,
        CancellationToken token);
}