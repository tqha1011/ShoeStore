using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using Microsoft.AspNetCore.SignalR;
using ShoeStore.Api.Hubs;
using ShoeStore.Application.DTOs.CheckOutDTOs;
using ShoeStore.Application.DTOs.HubDTOs;
using ShoeStore.Application.Interface.CheckoutInterface;
using ShoeStore.Application.Interface.Hub;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for handling payment webhook notifications from SePay payment gateway.
/// </summary>
/// <remarks>
///     ⚠️ IMPORTANT: This is a WEBHOOK endpoint designed for server-to-server communication with the SePay payment
///     provider.
///     **NOT intended for direct frontend use.**
///     This controller receives asynchronous payment notification callbacks from SePay after payment transactions.
///     It validates the incoming webhook request using API key authentication, processes the payment status,
///     and updates order information in the system.
///     Frontend developers should integrate with the payment system through the checkout flow (CheckOutController),
///     not by calling these endpoints directly.
/// </remarks>
/// <param name="configuration">Configuration service for retrieving SePay API credentials.</param>
/// <param name="paymentService">Service for processing payment webhook data and updating payment records.</param>
[Route("api/payment")]
[ApiController]
[EnableRateLimiting("limit-per-user")]
public class PaymentController(
    IConfiguration configuration,
    IPaymentService paymentService,
    IHubContext<NotifyHub, INotifyHubClient> hubContext) : ControllerBase
{
    /// <summary>
    ///     Webhook endpoint for receiving payment notifications from SePay payment gateway.
    /// </summary>
    /// <remarks>
    ///     ⚠️ **WEBHOOK ENDPOINT - Not for Frontend Use**
    ///     This endpoint is called by the SePay payment service to notify the application of payment transaction results.
    ///     It should NOT be called directly by frontend applications or client code.
    ///     **Authentication:**
    ///     - Requires `Authorization` header with format: `Apikey {apiKey}`
    ///     - The API key is retrieved from configuration (`Sepay:ApiKey`)
    ///     - Invalid or missing tokens result in 401 Unauthorized responses
    ///     **Request Body (from SePay):**
    ///     - <c>SepayWebhookDto</c>: Contains transaction details including:
    ///     - Transaction ID from SePay
    ///     - Payment status (success/failure)
    ///     - Amount paid
    ///     - Order reference
    ///     - Customer information
    ///     **Important Behavior:**
    ///     - Returns HTTP 200 OK regardless of payment processing success to acknowledge webhook receipt to SePay
    ///     - If validation succeeds but payment processing fails (e.g., order not found, insufficient amount),
    ///     still returns 200 OK with message indicating processing failure
    ///     - This prevents SePay from retrying the webhook due to application errors
    ///     - The `success` field in response indicates SePay gateway communication status, not business logic success
    ///     **Flow:**
    ///     1. SePay initiates payment transaction with customer
    ///     2. Customer completes payment on SePay gateway
    ///     3. SePay sends webhook notification to this endpoint
    ///     4. Endpoint validates API key
    ///     5. Endpoint processes payment and updates order status
    ///     6. Returns 200 OK to confirm webhook receipt
    /// </remarks>
    /// <param name="request">The webhook request body containing SePay payment transaction details.</param>
    /// <param name="token">Cancellation token for the async operation.</param>
    /// <response code="200">
    ///     Webhook received and processed successfully. Returns success status (note: this means webhook was
    ///     received, not that payment succeeded).
    /// </response>
    /// <response code="400">Bad request; invalid webhook payload format or missing required fields.</response>
    /// <response code="401">Unauthorized; missing or invalid Authorization header with SePay API key.</response>
    /// <response code="429">Too many requests; rate limit exceeded for webhook requests.</response>
    /// <returns>
    ///     An action result with HTTP 200 status and a JSON response containing:
    ///     - <c>success</c>: boolean indicating webhook receipt status (true = received, false = validation failed)
    ///     - <c>message</c>: descriptive message about the webhook processing result
    ///     Example success response:
    ///     {
    ///     "success": true,
    ///     "message": "Payment successfully processed"
    ///     }
    ///     Example processing failure response (still 200 OK):
    ///     {
    ///     "success": true,
    ///     "message": "Payment process failed due to some reasons"
    ///     }
    /// </returns>
    [HttpPost("sepay-webhook")]
    public async Task<IActionResult> SepayPayment([FromBody] SepayWebhookDto request, CancellationToken token)
    {
        var sepayKey = configuration.GetValue<string>("Sepay:ApiKey");
        if (!Request.Headers.TryGetValue("Authorization", out var headerToken))
            return Unauthorized(new
            {
                success = false,
                message = "Expect Authorize header token"
            });

        var expectedToken = $"Apikey {sepayKey}";
        if (headerToken != expectedToken)
            return Unauthorized(new
            {
                success = false,
                message = "Invalid token"
            });

        var isProcessed = await paymentService.ProcessSepayWebhookAsync(request, token);
        if (!isProcessed)
            // return 200 Ok status to notify Sepay that the connection is successful,
            // but the payment processing failed (e.g., insufficient amount, order not found)
            return Ok(new
            {
                success = true,
                message = "Payment process failed due to some reasons"
            });

        if (request.Code != null)
        {
            var orderCode = request.Code;
            var amount = request.TransferAmount;
            var message =
                $"Payment of {amount:#,##0} VND for order #{orderCode} has been successfully received.";
            var paymentNotification = new PaymentNotificationDto(message, amount, orderCode, true, DateTime.UtcNow);

            await hubContext.Clients.Group(orderCode).ReceivePaymentNotification(paymentNotification);
        }

        return Ok(new
        {
            success = true,
            message = "Payment successfully processed"
        });
    }
}