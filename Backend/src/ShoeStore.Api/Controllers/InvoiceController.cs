using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.SignalR;
using ShoeStore.Api.Hubs;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.Interface.Hub;
using ShoeStore.Application.Interface.InvoiceInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Provides invoice endpoints for authenticated users to track and update their own orders.
/// </summary>
/// <remarks>
///     All endpoints in this controller require the <c>User</c> role.
///     Successful responses return a JSON envelope with <c>message</c> and optional <c>data</c>.
/// </remarks>
/// <param name="invoiceService">Service used to query and update invoice data.</param>
[ApiController]
[Route("api/invoice/user")]
[Authorize(Roles = "User")]
public class InvoiceController(
    IInvoiceService invoiceService,
    IHubContext<NotifyHub, INotifyHubClient> hubContext) : ControllerBase
{
    /// <summary>
    ///     Gets a paginated list of invoices for the current user.
    /// </summary>
    /// <param name="request">
    ///     Query filters and paging values such as page index, page size, and optional invoice criteria.
    /// </param>
    /// <param name="token">Cancellation token from the HTTP request pipeline.</param>
    /// <response code="200">Invoices are returned successfully in <c>data</c>.</response>
    /// <response code="400">The query payload is invalid.</response>
    /// <response code="401">The caller is not authenticated.</response>
    /// <response code="403">The caller is authenticated but not allowed to access this resource.</response>
    /// <response code="404">No invoice matches the requested criteria.</response>
    /// <response code="500">Unexpected server-side error while processing the request.</response>
    [HttpGet("get-all")]
    [ProducesResponseType(typeof(PageResult<InvoiceResponseDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status403Forbidden)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    public async Task<IActionResult> GetInvoice([FromQuery] InvoiceRequestDto request, CancellationToken token)
    {
        var result = await invoiceService.GetInvoiceAsync(request, token);

        return result.Match<IActionResult>(
            pageResult => Ok(new
            {
                message = "Get invoices successfully",
                data = pageResult
            }),
            errors => errors[0].Code switch
            {
                "Invoice.NotFound" => NotFound(new
                {
                    message = "Invoice not found",
                    description = errors[0].Description
                }),


                "Invoice.Forbidden" => StatusCode(StatusCodes.Status403Forbidden, new
                {
                    code = "Invoice.Forbidden",
                    message = "You do not have permission to access this invoice.",
                    description = errors[0].Description
                }),


                "Invoice.BadRequest" => BadRequest(new
                {
                    message = "Invalid request data",
                    description = errors[0].Description
                }),


                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An unexpected error occurred. Please try again later",
                    description = errors[0].Description
                })
            }
        );
    }

    /// <summary>
    ///     Gets detailed information for a specific invoice.
    /// </summary>
    /// <param name="invoiceGuid">The invoice identifier.</param>
    /// <param name="token">Cancellation token from the HTTP request pipeline.</param>
    /// <response code="200">Invoice detail is returned successfully in <c>data</c>.</response>
    /// <response code="401">The caller is not authenticated.</response>
    /// <response code="403">The caller is authenticated but not allowed to access this resource.</response>
    /// <response code="404">The specified invoice detail is not found.</response>
    /// <response code="500">Unexpected server-side error while processing the request.</response>
    [HttpGet("{invoiceGuid}/details")]
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status403Forbidden)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    public async Task<IActionResult> GetDetails(Guid invoiceGuid, CancellationToken token)
    {
        var result = await invoiceService.GetInvoiceDetailAsync(invoiceGuid, token);

        return result.Match<IActionResult>(
            details => Ok(new
            {
                message = "Get invoice details successfully",
                data = details
            }),
            errors => errors[0].Code switch
            {
                "InvoiceDetail.NotFound" => NotFound(new
                {
                    message = "Invoice details not found",
                    description = errors[0].Description
                }),

                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An unexpected error occurred. Please try again later",
                    description = errors[0].Description
                })
            }
        );
    }

    /// <summary>
    ///     Updates invoice status by user workflow rules.
    /// </summary>
    /// <param name="invoiceGuid">The invoice identifier to update.</param>
    /// <param name="request">The requested target status in the request body.</param>
    /// <param name="token">Cancellation token from the HTTP request pipeline.</param>
    /// <response code="200">Invoice status is updated successfully.</response>
    /// <response code="400">The requested status transition is invalid.</response>
    /// <response code="401">The caller is not authenticated.</response>
    /// <response code="403">The caller is authenticated but not allowed to access this resource.</response>
    /// <response code="404">The target invoice is not found.</response>
    /// <response code="500">Unexpected server-side error while processing the request.</response>
    [HttpPut("{invoiceGuid}/status")]
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status403Forbidden)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    public async Task<IActionResult> UpdateStatusByCustomer(Guid invoiceGuid, [FromBody] UpdateStateRequestDto request,
        CancellationToken token)
    {
        var result = await invoiceService.UpdateInvoiceStateByUserAsync(invoiceGuid, request, token);
        if (result.IsError)
        {
            var firstError = result.FirstError;
            return firstError.Code switch
            {
                "Invoice.NotFound" => NotFound(new
                {
                    message = "Invoice not found",
                    description = firstError.Description
                }),

                "Invoice.InvalidStatus" => BadRequest(new
                {
                    message = "Invalid invoice status",
                    description = firstError.Description
                }),

                "Invoice.Forbidden" => StatusCode(StatusCodes.Status403Forbidden, new
                {
                    message = "You do not have permission to change to this status",
                    description = firstError.Description
                }),

                "Invoice.Unauthorized" => Unauthorized(new
                {
                    message = "You do not have permission to access this invoice.",
                    description = firstError.Description
                }),

                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An unexpected error occurred. Please try again later",
                    description = firstError.Description
                })
            };
        }

        // Notify clients about the invoice status update
        await hubContext.Clients.Group("Admin")
            .ReceiveNotification(result.Value,request.Status);

        return Ok(new
        {
            message = "Invoice has been updated successfully"
        });
    }
}