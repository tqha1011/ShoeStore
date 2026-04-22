using ErrorOr;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.VoucherDtos;
using ShoeStore.Application.Interface.VoucherInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for managing vouchers in the system.
///     Provides endpoints for voucher creation, retrieval, update, and deletion (Admin only).
/// </summary>
/// <param name="voucherService">Service for handling voucher logic operations.</param>
[ApiController]
[Route("api/admin/vouchers")]
[Authorize(Roles = "Admin")]
public class VoucherController(IVoucherService voucherService) : ControllerBase
{
    /// <summary>
    ///     Creates a new voucher for the store.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization.
    ///     The request body should include:
    ///     - <c>VoucherName</c>: Name of the voucher
    ///     - <c>Discount</c>: Value of the discount
    ///     - <c>DiscountType</c>: Type of discount (Percentage/FixedAmount)
    ///     - <c>TotalQuantity</c>: Number of vouchers available
    ///     - <c>ValidFrom/ValidTo</c>: Expiration dates
    /// </remarks>
    /// <param name="createVoucherDto">Data transfer object containing voucher creation details.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="201">Voucher created successfully.</response>
    /// <response code="400">Bad request; invalid voucher data provided.</response>
    /// <response code="401">Unauthorized; user must be authenticated with Admin role.</response>
    /// <response code="500">Internal server error; an unexpected error occurred.</response>
    /// <returns>An action result with status 201 (Created) on success, or an error response.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status201Created)]
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
                detail = errors[0].Description
            }));
    }

    /// <summary>
    ///     Updates an existing voucher's details.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization.
    ///     Updates the specified voucher with the provided information.
    /// </remarks>
    /// <param name="voucherGuid">The unique identifier (GUID) of the voucher to update.</param>
    /// <param name="updateVoucherDto">Data transfer object containing updated voucher details.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Voucher updated successfully.</response>
    /// <response code="400">Bad request; invalid update data provided.</response>
    /// <response code="401">Unauthorized; user must be authenticated with Admin role.</response>
    /// <response code="404">Not found; the voucher with the specified ID does not exist.</response>
    /// <response code="500">Internal server error; an unexpected error occurred.</response>
    /// <returns>An action result with status 200 (OK) on success, or an error response.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPut("{voucherGuid}")]
    public async Task<IActionResult> UpdateVoucher(Guid voucherGuid, [FromBody] UpdateVoucherDto updateVoucherDto,
        CancellationToken token)
    {
        var result = await voucherService.UpdateVoucherAsync(voucherGuid, updateVoucherDto, token);
        return result.Match<IActionResult>(
            _ => Ok(new { message = "Voucher updated successfully" }),
            errors => errors[0].Code switch
            {
                "VOUCHER_NOT_FOUND" => NotFound(new
                {
                    message = "Voucher not found",
                    detail = errors[0].Description
                }),
                _ => BadRequest(new
                {
                    message = "Failed to update voucher",
                    detail = errors[0].Description
                })
            });
    }

    /// <summary>
    ///     Deletes a specific voucher from the system (soft delete).
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization.
    ///     Performs a soft delete by marking the voucher as deleted.
    /// </remarks>
    /// <param name="voucherGuid">The unique identifier (GUID) of the voucher to delete.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Voucher deleted successfully.</response>
    /// <response code="401">Unauthorized; user must be authenticated with Admin role.</response>
    /// <response code="404">Not found; the voucher with the specified ID does not exist.</response>
    /// <response code="500">Internal server error; an unexpected error occurred.</response>
    /// <returns>An action result with status 200 (OK) on success, or an error response.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpDelete("{voucherGuid}")]
    public async Task<IActionResult> DeleteVoucher(Guid voucherGuid, CancellationToken token)
    {
        var result = await voucherService.DeleteVoucherByGuidAsync(voucherGuid, token);
        return result.Match<IActionResult>(
            _ => Ok(new { message = "Voucher deleted successfully" }),
            errors => errors[0].Code switch
            {
                "VOUCHER_NOT_FOUND" => NotFound(new
                {
                    message = "Voucher not found",
                    detail = errors[0].Description
                }),
                _ => BadRequest(new
                {
                    message = "Failed to delete voucher",
                    detail = errors[0].Description
                })
            });
    }

    /// <summary>
    ///     Deletes all expired vouchers from the system (soft delete).
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization.
    ///     Identifies and soft deletes all vouchers whose expiration date has passed.
    /// </remarks>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Expired vouchers deleted successfully.</response>
    /// <response code="400">Bad request; failed to delete expired vouchers or no expired vouchers found.</response>
    /// <response code="401">Unauthorized; user must be authenticated with Admin role.</response>
    /// <response code="500">Internal server error; an unexpected error occurred.</response>
    /// <returns>An action result with status 200 (OK) on success, or an error response.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpDelete("expire")]
    public async Task<IActionResult> DeleteExpiredVouchers(CancellationToken token)
    {
        var result = await voucherService.DeleteVoucherExpireAsync(token);
        return result.Match<IActionResult>(
            _ => Ok(new { message = "Expired vouchers deleted successfully" }),
            errors => BadRequest(new
            {
                message = "Failed to delete expired vouchers",
                detail = errors[0].Description
            }));
    }

    /// <summary>
    ///     Retrieves a paginated list of vouchers for administrative purposes.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization.
    ///     Provides a list of vouchers with detailed information relevant for administrators.
    /// </remarks>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Vouchers retrieved successfully.</response>
    /// <response code="401">Unauthorized; user must be authenticated with Admin role.</response>
    /// <response code="404">Not found; no vouchers found in the system.</response>
    /// <response code="500">Internal server error; an unexpected error occurred.</response>
    /// <returns>An action result containing a paginated list of vouchers on success, or an error response.</returns>
    [ProducesResponseType(typeof(PageResult<ResponseVoucherAdminDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet]
    public async Task<IActionResult> GetVouchersForAdmin(CancellationToken token)
    {
        var result = await voucherService.GetVoucherForAdminAsync(token);
        return result.Match<IActionResult>(
            vouchers => Ok(vouchers),
            errors => errors[0].Code switch
            {
                "NO_VOUCHERS_FOUND" => NotFound(new
                {
                    message = "No vouchers found",
                    detail = errors[0].Description
                }),
                _ => BadRequest(new
                {
                    message = "Failed to retrieve vouchers",
                    detail = errors[0].Description
                })
            });
    }

    /// <summary>
    ///     Retrieves all active and non-deleted vouchers in the system.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization.
    /// </remarks>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">All vouchers retrieved successfully.</response>
    /// <response code="401">Unauthorized; user must be authenticated with Admin role.</response>
    /// <response code="404">Not found; no vouchers found in the system.</response>
    /// <response code="500">Internal server error; an unexpected error occurred.</response>
    /// <returns>An action result containing a list of vouchers on success, or an error response.</returns>
    [ProducesResponseType(typeof(PageResult<ResponseVoucherAdminDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("all")]
    public async Task<IActionResult> GetAllVouchers(CancellationToken token)
    {
        var result = await voucherService.GetAllVouchersAsync(token);
        return result.Match<IActionResult>(
            vouchers => Ok(vouchers),
            errors => errors[0].Code switch
            {
                "NO_VOUCHERS_FOUND" => NotFound(new
                {
                    message = "No vouchers found",
                    detail = errors[0].Description
                }),
                _ => BadRequest(new
                {
                    message = "Failed to retrieve all vouchers",
                    detail = errors[0].Description
                })
            });
    }
}
