using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class ChatSessionService(
    IChatSessionRepository chatSessionRepository,
    IUserRepository userRepository,
    IUnitOfWork unitOfWork)
    : IChatSessionService
{
    public async Task<ErrorOr<PageResult<ChatSessionResponseDto>>> GetChatSessionsAsync(Guid publicUserId,
        CancellationToken token, int pageNumber = 1,
        int pageSize = 10)
    {
        var user = await userRepository.GetUserByPublicIdAsync(publicUserId, token, false);
        if (user == null) return Error.NotFound("User.NotFound", "User not found.");

        var query = chatSessionRepository.GetChatSessionsByUserId(user.Id);

        var totalCount = await query.CountAsync(token);

        query = query.OrderByDescending(s => s.CreatedAt).ApplyPagination(pageNumber, pageSize);

        var sessions = await query.Select(s => new ChatSessionResponseDto(
            s.PublicId,
            publicUserId,
            s.Title ?? string.Empty,
            s.CreatedAt
        )).ToListAsync(token);

        var pageResult = new PageResult<ChatSessionResponseDto>
        {
            Items = sessions,
            TotalCount = totalCount,
            PageNumber = pageNumber,
            PageSize = pageSize
        };
        return pageResult;
    }

    public async Task<ErrorOr<CreateSessionResponseDto>> CreateSessionAsync(Guid publicUserId, CancellationToken token)
    {
        var user = await userRepository.GetUserByPublicIdAsync(publicUserId, token, false);
        if (user == null) return Error.NotFound("User.NotFound", "User not found.");
        var newSession = new ChatSession
        {
            Title = "New Chat",
            UserId = user.Id,
            CreatedAt = DateTime.UtcNow
        };
        chatSessionRepository.Add(newSession);
        await unitOfWork.SaveChangesAsync(token);
        return new CreateSessionResponseDto(newSession.PublicId, newSession.Title);
    }
}