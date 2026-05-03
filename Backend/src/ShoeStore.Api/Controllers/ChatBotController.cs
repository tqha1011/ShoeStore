using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using ShoeStore.Application.Interface;

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
public class ChatBotController(IChatBotService chatBotService) : ControllerBase
{
    /// <summary>
    ///     Streams a generated campaign proposal.
    /// </summary>
    /// <remarks>
    ///     The response is streamed as server-sent events (text/event-stream).
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
        Response.Headers.Append("Content-Type", "text/event-stream");

        var stream = chatBotService.GenerateCampaignAsync(cancellationToken);
        await foreach (var chunk in stream)
        {
            await Response.WriteAsync(chunk, cancellationToken);
            await Response.Body.FlushAsync(cancellationToken);
        }
    }
}