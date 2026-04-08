using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.Interface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for handling checkout and order placement operations.
///     Provides endpoints for preparing checkout sessions and placing orders with proper authorization.
///     All operations require user authorization and enforce rate-limiting to prevent abuse.
/// </summary>
/// <param name="checkOutService">Service for handling checkout and order operations.</param>
[Route("api/checkout")]
[ApiController]
[Authorize]
public class CheckOutController(ICheckOutService checkOutService) : ControllerBase
{
    /// <summary>
    ///     Prepares a checkout session by validating all items and calculating order totals.
    /// </summary>
    /// <remarks>
    ///     Requires user authorization (extracted from JWT token in claims) and a request body with:
    ///     - <c>checkOutList</c>: array of checkout items containing variant IDs and quantities
    ///     Validates that all product variants exist and have sufficient stock.
    ///     Calculates subtotals, discounts, tax, and shipping costs for the frontend.
    ///     Rate-limited per user to prevent abuse.
    /// </remarks>
    /// <param name="checkOutList">List of checkout request items to prepare.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Checkout prepared successfully. Returns order summary with pricing details.</response>
    /// <response code="404">Not found; one or more product variants do not exist.</response>
    /// <response code="401">Unauthorized; user must be logged in with a valid JWT token.</response>
    /// <response code="429">Too many requests; rate limit exceeded for this user.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>
    ///     An action result containing the checkout summary with pricing and item details on success, or an error
    ///     response describing what went wrong.
    /// </returns>
    [HttpPost("prepare")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> PrepareCheckOut(List<CheckOutRequestDto> checkOutList, CancellationToken token)
    {
        var validUser = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (validUser == null)
            return Unauthorized(new
            {
                message = "You are not authorized to perform this action.",
                description = "Please login to your account and try again."
            });
        var result = await checkOutService.PrepareCheckOutAsync(checkOutList, token);

        var response = result.Match<IActionResult>(
            responseDto => Ok(responseDto),
            errors => errors[0].Code switch
            {
                "Variant.NotFound" => NotFound(new
                {
                    message = "Your variant does not exist.",
                    description = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "Something went wrong. Please try again later.",
                    description = errors[0].Description
                })
            });
        return response;
    }

    /// <summary>
    ///     Places an order with payment processing and invoice generation.
    /// </summary>
    /// <remarks>
    ///     Requires user authorization (extracted from JWT token in claims) and a request body with:
    ///     - <c>placeOrderRequestDto</c>: order details including items, shipping address, and payment information
    ///     - <c>fromUserCart</c>: boolean flag indicating whether order is from saved cart items or direct purchase
    ///     Validates all cart items exist and have sufficient stock.
    ///     Processes payment through the payment service.
    ///     Generates invoice and confirmation for the order.
    ///     Rate-limited per user to prevent duplicate orders.
    /// </remarks>
    /// <param name="placeOrderRequestDto">The complete order details for placement.</param>
    /// <param name="fromUserCart">Flag indicating if order is from user's saved cart (true) or direct checkout (false).</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="201">Order placed successfully. Returns order confirmation and invoice details.</response>
    /// <response code="404">Not found; one or more product variants or cart items do not exist.</response>
    /// <response code="401">Unauthorized; user must be logged in with a valid JWT token.</response>
    /// <response code="429">Too many requests; rate limit exceeded for this user.</response>
    /// <response code="500">Internal server error; invoice creation failed or payment processing failed.</response>
    /// <returns>
    ///     An action result containing the order confirmation on success, or an error response describing what went
    ///     wrong.
    /// </returns>
    [HttpPost("place-order")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> PlaceOrder(PlaceOrderRequestDto placeOrderRequestDto, bool fromUserCart,
        CancellationToken token)
    {
        var validUser = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (validUser == null || !Guid.TryParse(validUser, out var publicUserId))
            return Unauthorized(new
            {
                message = "You are not authorized to perform this action.",
                description = "Please login to your account and try again."
            });

        var result =
            await checkOutService.PlaceOrderAsync(placeOrderRequestDto, publicUserId, fromUserCart, token);

        var response = result.Match<IActionResult>(
            _ => Created(),
            errors => errors[0].Code switch
            {
                "Variant.NotFound" => NotFound(new
                {
                    message = "One or more variants in your order do not exist.",
                    description = errors[0].Description
                }),
                "CartItem.NotFound" => NotFound(new
                {
                    message = "One or more items in your cart do not exist.",
                    description = errors[0].Description
                }),
                "Voucher.NotFound" => NotFound(new
                {
                    message = "One or more your voucher are expired or used",
                    description = errors[0].Description
                }),
                "InvoiceCreation.Failed" => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "Failed to create invoice for your order.",
                    description = errors[0].Description
                }),
                "Checkout.Concurrency" => Conflict(new
                {
                    message = "Your product had been sold. Please try again.",
                    description = errors[0].Description
                }),
                "Stock.NotEnough" => BadRequest(new
                {
                    message = "Shop's stock not enough. Please try again later.",
                    description = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "Something went wrong. Please try again later.",
                    description = errors[0].Description
                })
            });
        return response;
    }
}