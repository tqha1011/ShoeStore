using Microsoft.AspNetCore.SignalR;
using ShoeStore.Application.DTOs.HubDTOs;
using ShoeStore.Application.Interface.Hub;

namespace ShoeStore.Api.Hubs;

/// <summary>
///     SignalR Hub for real-time payment notifications via WebSocket.
///     Endpoint: <c>/notify-hub</c> | Requires JWT authentication.
/// </summary>
/// <remarks>
///     <strong>Frontend Setup (Kotlin Compose):</strong>
///     <code language="kotlin">
/// // 1. Initialize HubConnection with JWT token
/// val hubConnection = HubConnectionBuilder()
///     .withUrl("https://your-api.com/notify-hub") {
///         val token = sharedPreferences.getString("jwt_token", "")
///         headers["Authorization"] = "Bearer $token"
///     }
///     .withAutomaticReconnect()
///     .build()
/// 
/// hubConnection.start()
///     .blockingAwait()
/// 
/// // 2. Join order group after placing order
/// hubConnection.invoke("JoinInvoiceGroup", orderCode)
///     .blockingAwait()
/// 
/// // 3. Listen for payment notifications
/// hubConnection.on("ReceiveNotification", { notification: PaymentNotification ->
///     // notification: { message, amount, orderCode, isSuccess, timestamp }
///     Log.d("Payment", "Received: ${notification.message}")
///     // Update UI state: paymentStatus = PaymentStatus.Success
/// }, PaymentNotification::class.java)
/// 
/// // 4. Leave group when navigating away
/// hubConnection.invoke("LeftInvoiceGroup", orderCode)
///     .blockingAwait()
/// 
/// // 5. Disconnect when done
/// hubConnection.stop().blockingAwait()
/// </code>
/// </remarks>
public class NotifyHub : Hub<INotifyHubClient>
{
    /// <summary>
    ///     Adds the client connection to an order notification group.
    ///     After calling this, the client will receive payment notifications for this order.
    /// </summary>
    /// <remarks>
    ///     <strong>Usage:</strong> Call immediately after placing an order to start listening for payment updates.
    ///     The client remains in the group until <see cref="LeaveInvoiceGroup" /> is invoked or connection is lost.
    /// </remarks>
    /// <param name="orderCode">The order code to join (e.g., "DH000001").</param>
    public async Task JoinInvoiceGroup(string orderCode)
    {
        var user = Context.ConnectionId;
        await Groups.AddToGroupAsync(user, orderCode);
    }

    /// <summary>
    ///     Removes the client connection from an order notification group.
    ///     After calling this, the client will no longer receive payment notifications for this order.
    /// </summary>
    /// <remarks>
    ///     <strong>Usage:</strong> Call when user navigates away from order details page or closes the order screen.
    /// </remarks>
    /// <param name="orderCode">The order code to leave (e.g., "DH000001").</param>
    public async Task LeaveInvoiceGroup(string orderCode)
    {
        var user = Context.ConnectionId;
        await Groups.RemoveFromGroupAsync(user, orderCode);
    }
}