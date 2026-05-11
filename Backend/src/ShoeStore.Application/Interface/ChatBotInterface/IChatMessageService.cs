using ErrorOr;
using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IChatMessageService
{
    Task<ErrorOr<List<MessageResponseDto>>> GetMessagesInSessionAsync(Guid sessionPublicId, DateTime? cursor,
        CancellationToken token);
}