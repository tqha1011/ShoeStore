using Microsoft.AspNetCore.SignalR;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface.Hub;

namespace ShoeStore.Api.Hubs;

/// <summary>
///     SignalR notifier for sending chatbot-related responses to admin clients.
/// </summary>
/// <param name="hubContext">SignalR hub context used to publish notifications.</param>
public class NotifyBotResponse(IHubContext<NotifyBotHub,INotifyBotHub> hubContext) : INotifyBotResponse
{
    /// <summary>
    ///     Sends a variant draft result to the admin client group for the specified user.
    /// </summary>
    /// <param name="result">The variant draft result payload.</param>
    /// <param name="publicUserid">The public user identifier used to target the admin group.</param>
    public async Task NotifyAddVariantDraftAsync(AddVariantResultDto result,Guid publicUserid)
    {
        await hubContext.Clients.Group($"Admin-{publicUserid}").NotifyAddVariantResponse(result);
    }
}