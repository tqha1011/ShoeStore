using ErrorOr;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.VoucherDtos;
using ShoeStore.Application.Interface.VoucherInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for managing vouchers in the system.
///     Provides endpoints for voucher creation and management (Admin only).
/// </summary>
/// <param name="voucherService">Service for handling voucher logic operations.</param>
[ApiController]
[Route("api/admin/vouchers")]
// [Authorize(Roles = "Admin")]
public class VoucherController(IVoucherService voucherService) : ControllerBase
{
    /// <summary>
    ///     Creates a new voucher for the store.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization.
    ///     The request body should include:
    ///     - VoucherName: Name of the voucher
    ///     - Discount: Value of the discount
    ///     - DiscountType: Type of discount (Percentage/FixedAmount)
    ///     - TotalQuantity: Number of vouchers available
    ///     - ValidFrom/ValidTo: Expiration dates
    /// </remarks>
    /// <param name="createVoucherDto">Data transfer object containing voucher creation details.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="201">Voucher created successfully.</response>
    /// <response code="400">Bad request; invalid voucher data provided.</response>
    /// <response code="401">Unauthorized; user must be authenticated with Admin role.</response>
    /// <response code="500">Internal server error; an unexpected error occurred.</response>
    /// <returns>An action result with status 201 (Created) on success, or an error response.</returns>
    [ProducesResponseType(StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]

    [HttpPost]
    public async Task<IActionResult> CreateVoucher([FromBody] CreateVoucherDto createVoucherDto, CancellationToken token)
    {
        var result = await voucherService.CreateVoucherAsync(createVoucherDto, token);

        return result.Match<IActionResult>(
            _ => Created("", new { message = "Voucher created successfully" }),
            errors => BadRequest(new
            {
                message = "Failed to create voucher",
                details = errors
            }));
    }

    [HttpPut("{voucherGuid}")]
    public async Task<IActionResult> UpdateVoucher(Guid voucherGuid, [FromBody] UpdateVoucherDto updateVoucherDto, CancellationToken token)
    {
        var result = await voucherService.UpdateVoucherAsync(voucherGuid, updateVoucherDto, token);
        return result.Match<IActionResult>(
            _ => Ok(new { message = "Voucher updated successfully" }),
            errors => errors.Any(e => e.Type == ErrorType.NotFound)
                ? NotFound(new { message = "Voucher not found" })
                : BadRequest(new
                {
                    message = "Failed to update voucher",
                    details = errors
                }));
    }
}
