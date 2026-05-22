using System.Security.Claims;
using Microsoft.AspNetCore.SignalR;
using ShoeStore.Application.Interface.Hub;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Api.Hubs;

/// <summary>
///     SignalR hub for admin-only bot draft notifications.
/// </summary>
/// <remarks>
///     <strong>Frontend Setup (Kotlin Compose):</strong>
///     <code language="kotlin">
/// // 1. Initialize HubConnection with JWT token
/// val hubConnection = HubConnectionBuilder()
///     .withUrl("https://your-api.com/notify-bot-hub") {
///         val token = sharedPreferences.getString("jwt_token", "")
///         headers["Authorization"] = "Bearer $token"
///     }
///     .withAutomaticReconnect()
///     .build()
/// 
/// hubConnection.start().blockingAwait()
/// 
/// // 2. Listen for admin draft updates
/// hubConnection.on("NotifyAddVariantResponse", { result: AddVariantResult ->
///     // result: { status, message, data }
///     // Update UI state with the draft payload
/// }, AddVariantResult::class.java)
/// 
/// // 3. Disconnect when done
/// hubConnection.stop().blockingAwait()
/// </code>
/// </remarks>
public class NotifyBotHub : Hub<INotifyBotHub>
{
    /// <summary>
    ///     Registers the connecting admin into the per-admin notification group.
    /// </summary>
    /// <remarks>
    ///     <strong>Behavior:</strong>
    ///     <list type="bullet">
    ///         <item>
    ///             <description>Reads the user id and role from JWT claims on the connection.</description>
    ///         </item>
    ///         <item>
    ///             <description>If the user is <c>Admin</c>, adds them to the <c>Admin-{userId}</c> group.</description>
    ///         </item>
    ///     </list>
    ///     <strong>Frontend Usage:</strong> This is called automatically by SignalR on connect.
    ///     No explicit client call is required beyond establishing the connection.
    /// </remarks>
    /// <returns>A task that represents the asynchronous connection initialization.</returns>
    public override async Task OnConnectedAsync()
    {
        var userId = Context.User?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
        var connectionId = Context.ConnectionId;
        var role = Context.User?.FindFirst(ClaimTypes.Role)?.Value;
        if (role == nameof(UserRole.Admin)) await Groups.AddToGroupAsync(connectionId, $"Admin-{userId}");
        await base.OnConnectedAsync();
    }
}