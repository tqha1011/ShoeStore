using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IChatSessionService
{
    Task<ErrorOr<PageResult<ChatSessionResponseDto>>> GetChatSessionsAsync(Guid publicUserId, CancellationToken token,
        int pageNumber = 1,
        int pageSize = 10);

    Task<ErrorOr<CreateSessionResponseDto>> CreateSessionAsync(Guid publicUserId, CancellationToken token);
}