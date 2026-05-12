using System.Security.Claims;
using Asp.Versioning;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface.ChatBotInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for retrieving chat messages in a session for authenticated users.
/// </summary>
/// <remarks>
///     Provides a cursor-based pagination endpoint to load older messages as the user scrolls.
/// </remarks>
/// <param name="chatMessageService">Service for querying chat message data.</param>
[Route("api/v{version:apiVersion}/chat-message")]
[ApiController]
[ApiVersion(1)]
[Authorize]
public class ChatMessageController(IChatMessageService chatMessageService) : ControllerBase
{
    /// <summary>
    ///     Retrieves messages in a chat session using optional cursor-based pagination.
    /// </summary>
    /// <remarks>
    ///     Requires an authenticated user and a valid <c>publicSessionId</c> query value.
    ///     Use <c>cursor</c> to fetch older messages created before the cursor timestamp.
    /// </remarks>
    /// <param name="publicSessionId">The public GUID identifier of the chat session.</param>
    /// <param name="cursor">Optional timestamp cursor to load older messages.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Messages retrieved successfully.</response>
    /// <response code="401">Unauthorized; user is not authenticated.</response>
    /// <response code="404">Not found; chat session does not exist.</response>
    /// <response code="500">Internal server error; failed to retrieve messages.</response>
    /// <returns>A list of chat messages for the session.</returns>
    [ProducesResponseType(typeof(List<MessageResponseDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet]
    public async Task<IActionResult> GetMessageInSession([FromQuery] Guid publicSessionId, [FromQuery] DateTime? cursor,
        CancellationToken token)
    {
        var validUser = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (validUser == null || !Guid.TryParse(validUser, out var publicUserId))
        {
            return Unauthorized();
        }
        var result = await chatMessageService.GetMessagesInSessionAsync(publicSessionId,publicUserId, cursor, token);

        var response = result.Match<IActionResult>(
            messages => Ok(messages),
            errors => errors[0].Code switch
            {
                "ChatSession.NotFound" => NotFound(new
                {
                    message = "Chat session not found.",
                    description = "The specified chat session does not exist."
                }),
                "User.NotFound" => NotFound(new
                {
                    message = "User not found.",
                    description = "Please login or create an account"
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An error occurred while retrieving messages.",
                    description = errors[0].Description
                })
            });
        return response;
    }
}