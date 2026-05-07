using Asp.Versioning;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using ShoeStore.Application.DTOs.ChatBotDTOs;
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
[Route("api/v{version:apiVersion}/chatbot")]
[ApiVersion(1)]
[ApiController]
[EnableRateLimiting("limit-per-user")]
[Authorize]
public class ChatBotController(IChatBotService chatBotService) : ControllerBase
{
    /// <summary>
    ///     Streams a generated campaign proposal.
    /// </summary>
    /// <remarks>
    ///     The response is streamed as Server-Sent Events (SSE) with <c>text/event-stream</c> content type.
    ///     The request requires an authenticated user.
    ///     <strong>
    ///         CRITICAL: The client MUST call the API to create a chat session and obtain a publicSessionId BEFORE calling
    ///         this endpoint.
    ///     </strong>
    /// </remarks>
    /// <param name="requestDto">Campaign generation inputs for the chatbot.</param>
    /// <param name="cancellationToken">Request cancellation token.</param>
    /// <response code="200">Campaign text stream started successfully.</response>
    /// <response code="401">Unauthorized; user is not authenticated.</response>
    /// <response code="500">Internal server error; generation failed.</response>
    /// <returns>Streaming SSE response of the campaign content.</returns>
    [ProducesResponseType(typeof(string), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost("generate-campaign")]
    public async Task GenerateCampaign(CreateCampaignRequestDto requestDto, CancellationToken cancellationToken)
    {
        var response = await chatBotService.GenerateCampaignAsync(requestDto, cancellationToken);

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

        SetSseHeaders();

        await foreach (var chunk in response.Value.WithCancellation(cancellationToken))
            await SendSseChunkAsync(chunk, cancellationToken);

        // send [DONE] event to indicate completion of the stream to the client
        await SendSseChunkAsync("[DONE]", cancellationToken);
    }

    /// <summary>
    ///     Streams chatbot responses for statistics-related questions in a session.
    /// </summary>
    /// <remarks>
    ///     The response is streamed as Server-Sent Events (SSE) with <c>text/event-stream</c> content type.
    ///     The request requires an authenticated user.
    /// </remarks>
    /// <param name="requestDto">The user's message and context for statistics inquiry.</param>
    /// <param name="publicSessionId">The public session identifier for the chat session.</param>
    /// <param name="cancellationToken">Request cancellation token.</param>
    /// <response code="200">Statistics response stream started successfully.</response>
    /// <response code="401">Unauthorized; user is not authenticated.</response>
    /// <response code="500">Internal server error; generation failed.</response>
    /// <returns>Streaming SSE response of the chatbot output.</returns>
    [HttpPost("chat-statistics")]
    public async Task ChatAskAboutStatistics([FromBody] ChatMessageRequestDto requestDto,
        [FromQuery] Guid publicSessionId, CancellationToken cancellationToken)
    {
        var response = await chatBotService.ChatAskAboutStatisticsAsync(publicSessionId, requestDto, cancellationToken);
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

        SetSseHeaders();

        await foreach (var chunk in response.Value.WithCancellation(cancellationToken))
            await SendSseChunkAsync(chunk, cancellationToken);
        await SendSseChunkAsync("[DONE]", cancellationToken);
    }

    private void SetSseHeaders()
    {
        Response.ContentType = "text/event-stream";
        Response.Headers.Append("Cache-Control", "no-cache");
        Response.Headers.Append("Connection", "keep-alive");
    }

    private async Task SendSseChunkAsync(string chunk, CancellationToken cancellationToken)
    {
        if (string.IsNullOrEmpty(chunk)) return;

        var sanitizedChunk = chunk.Replace("\n", "\ndata: ");
        await Response.WriteAsync($"data: {sanitizedChunk}\n\n", cancellationToken);
        await Response.Body.FlushAsync(cancellationToken);
    }
}