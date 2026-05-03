using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface.ChatBotInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for managing chat sessions for authenticated users.
/// </summary>
/// <remarks>
///     Provides endpoints to fetch user chat sessions with pagination.
/// </remarks>
/// <param name="chatSessionService">Service for querying chat session data.</param>
[Route("api/session")]
[ApiController]
[Authorize]
public class ChatSessionController(IChatSessionService chatSessionService) : ControllerBase
{
    /// <summary>
    ///     Retrieves chat sessions for the current authenticated user.
    /// </summary>
    /// <remarks>
    ///     Reads the user identifier from the authenticated principal and returns paged session data.
    /// </remarks>
    /// <param name="token">Cancellation token for the request.</param>
    /// <param name="pageNumber">Page number to retrieve (1-based).</param>
    /// <param name="pageSize">Number of items per page.</param>
    /// <response code="200">Chat sessions retrieved successfully.</response>
    /// <response code="401">Unauthorized; user is not authenticated.</response>
    /// <response code="404">Not found; user does not exist.</response>
    /// <response code="500">Internal server error; failed to retrieve sessions.</response>
    /// <returns>A paged result of chat sessions.</returns>
    [ProducesResponseType(typeof(PageResult<ChatSessionResponseDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet]
    public async Task<IActionResult> GetChatSessionsForUser(CancellationToken token,
        [FromQuery] int pageNumber = 1, [FromQuery] int pageSize = 10)
    {
        var validUser = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (validUser == null || !Guid.TryParse(validUser, out var publicUserId))
        {
            return Unauthorized();
        }
        var result = await chatSessionService.GetChatSessionsAsync(publicUserId, token, pageNumber, pageSize);
        var response = result.Match<IActionResult>(
            pageResult => Ok(pageResult),
            errors => errors[0].Code switch
            {
                "User.NotFound" => NotFound(new
                {
                    message = "User not found.",
                    description = "The specified user does not exist."
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An error occurred while retrieving chat sessions.",
                    description = errors[0].Description
                })
            });
        return response;
    }
}