using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using ShoeStore.Application.Interface.ChatBotInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Chatbot endpoints for streaming marketing campaign content.
/// </summary>
/// <remarks>
///     This controller exposes a rate-limited endpoint that streams campaign text
///     generated from current business statistics.
/// </remarks>
/// <param name="chatBotService">Application service that builds campaign output.</param>
[Route("api/chatbot")]
[ApiController]
[EnableRateLimiting("limit-per-user")]
[Authorize]
public class ChatBotController(IChatBotService chatBotService) : ControllerBase
{
    /// <summary>
    ///     Streams a generated campaign proposal.
    /// </summary>
    /// <remarks>
    ///     The response is streamed as server-sent events (text/plain).
    ///     The request requires an authenticated user.
    /// </remarks>
    /// <param name="cancellationToken">Request cancellation token.</param>
    /// <response code="200">Campaign text stream started successfully.</response>
    /// <response code="401">Unauthorized; user is not authenticated.</response>
    /// <response code="500">Internal server error; generation failed.</response>
    /// <returns>Streaming text response of the campaign content.</returns>
    [ProducesResponseType(typeof(string), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost("generate-campaign")]
    public async Task GenerateCampaign(CancellationToken cancellationToken)
    {
        var response = await chatBotService.GenerateCampaignAsync(cancellationToken);

        if (response.IsError)
        {
            Response.StatusCode = StatusCodes.Status500InternalServerError;
            Response.ContentType = "application/json";
            await Response.WriteAsJsonAsync(new
            {
                message = "Generation failed",
                detail = response.FirstError.Description
            }, cancellationToken);
            return;
        }

        Response.ContentType = "text/plain";
        Response.Headers.Append("Cache-Control", "no-cache");
        Response.Headers.Append("Connection", "keep-alive");

        await foreach (var chunk in response.Value.WithCancellation(cancellationToken))
        {
            await Response.WriteAsync(chunk, cancellationToken);
            await Response.Body.FlushAsync(cancellationToken);
        }
    }
}