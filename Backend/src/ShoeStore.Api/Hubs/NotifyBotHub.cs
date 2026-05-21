using System.Security.Claims;
using Microsoft.AspNetCore.SignalR;
using ShoeStore.Application.Interface.Hub;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Api.Hubs;

/// <summary>
/// 
/// </summary>
public class NotifyBotHub : Hub<INotifyBotHub>
{
    /// <summary>
    /// This method is called when a client connects to the hub. It checks the user's role and adds them to a group if they are an admin.
    /// </summary>
    public override async Task OnConnectedAsync()
    {
        var userId = Context.User?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        var user = Context.ConnectionId; 
        var role = Context.User?.FindFirst(ClaimTypes.Role)?.Value;
        if (role == nameof(UserRole.Admin))
        {
            await Groups.AddToGroupAsync(user, $"Admin-{userId}");
        }
        await base.OnConnectedAsync();
    }
}