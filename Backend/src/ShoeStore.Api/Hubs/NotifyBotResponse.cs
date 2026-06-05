using Microsoft.AspNetCore.SignalR;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface.Hub;

namespace ShoeStore.Api.Hubs;

/// <summary>
///     SignalR notifier for sending chatbot-related responses to admin clients.
/// </summary>
/// <param name="hubContext">SignalR hub context used to publish notifications.</param>
public class NotifyBotResponse(IHubContext<NotifyBotHub, INotifyBotHub> hubContext) : INotifyBotResponse
{
    /// <summary>
    ///     Sends a variant draft result to the admin client group for the specified user.
    /// </summary>
    /// <param name="result">The variant draft result payload.</param>
    /// <param name="publicUserid">The public user identifier used to target the admin group.</param>
    public async Task NotifyAddVariantDraftAsync(AddVariantResultDto result, Guid publicUserid)
    {
        Console.WriteLine(
            $"[SIGNALR SPEED TEST ADD] Dang phat loa den Group: Admin-{publicUserid} | Cuoc data Status: {result.Status} | Add thanh cong: {result.Variant?.ProductId}");
        await hubContext.Clients.Group($"Admin-{publicUserid}").NotifyAddVariantResponse(result);
    }

    /// <summary>
    ///     Sends product search results to the admin client group for the specified user.
    /// </summary>
    /// <param name="result">The product search result payload.</param>
    /// <param name="publicUserid">The public user identifier used to target the admin group.</param>
    public async Task NotifyProductSearchResultAsync(SearchResultDto result, Guid publicUserid)
    {
        var productName = string.Join(", ", result.Products.Select(p => p.ProductName));
        Console.WriteLine(
            $"[SIGNALR SPEED TEST SEARCH] Dang phat loa den Group: Admin-{publicUserid} | Cuoc data Status: {result.Status} |" +
            $"Cac san pham tim duoc: {productName}");
        await hubContext.Clients.Group($"Admin-{publicUserid}").NotifySearchResultAsync(result);
    }
}