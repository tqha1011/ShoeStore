using ErrorOr;
using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IChatMessageService
{
    Task<ErrorOr<List<MessageResponseDto>>> GetMessagesInSessionAsync(Guid sessionPublicId,Guid publicUserId,DateTime? cursor,
        CancellationToken token);
}