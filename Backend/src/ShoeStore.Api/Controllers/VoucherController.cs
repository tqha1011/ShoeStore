using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Application.Interface.VoucherInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Manages voucher administration endpoints.
/// </summary>
/// <remarks>
///     All endpoints in this controller require the <c>Admin</c> role.
/// </remarks>
/// <param name="voucherService">Service that handles voucher business logic.</param>
[ApiController]
[Route("api/admin/vouchers")]
[Authorize(Roles = "Admin")]
public class VoucherController(IVoucherService voucherService) : ControllerBase
{
    /// <summary>
    ///     Creates a new voucher.
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
    /// <param name="createVoucherDto">Voucher payload used to create a new voucher.</param>
    /// <param name="token">Cancellation token.</param>
    /// <response code="200">Voucher created successfully.</response>
    /// <response code="400">Voucher creation failed due to invalid input or business validation.</response>
    /// <response code="401">Caller is not authenticated as Admin.</response>
    /// <response code="500">Unhandled server error.</response>
    /// <returns>An <see cref="IActionResult" /> containing a success or error response.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost]
    public async Task<IActionResult> CreateVoucher([FromBody] CreateVoucherDto createVoucherDto,
        CancellationToken token)
    {
        var result = await voucherService.CreateVoucherAsync(createVoucherDto, token);

        var response = result.Match<IActionResult>(
            _ => Ok(new { message = "Voucher created and users notified" }),
            errors => BadRequest(new
            {
                message = "Failed to create voucher",
                detail = errors[0].Description
            })
        );
        return response;
    }

    /// <summary>
    ///     Updates an existing voucher.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization.
    ///     Updates the specified voucher with the provided information.
    /// </remarks>
    /// <param name="voucherGuid">The unique identifier (GUID) of the voucher to update.</param>
    /// <param name="updateVoucherDto">Data transfer object containing updated voucher details.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Voucher updated successfully.</response>
    /// <response code="400">Update request is invalid.</response>
    /// <response code="401">Caller is not authenticated as Admin.</response>
    /// <response code="404">Voucher was not found.</response>
    /// <response code="500">Unhandled server error.</response>
    /// <returns>An <see cref="IActionResult" /> with update result details.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPut("{voucherGuid}")]
    public async Task<IActionResult> UpdateVoucher(Guid voucherGuid, UpdateVoucherDto updateVoucherDto,
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
    ///     Soft deletes a voucher.
    /// </summary>
    /// <param name="voucherGuid">Public identifier of the voucher to delete.</param>
    /// <param name="token">Cancellation token.</param>
    /// <response code="200">Voucher deleted successfully.</response>
    /// <response code="401">Caller is not authenticated as Admin.</response>
    /// <response code="404">Voucher was not found.</response>
    /// <response code="500">Unhandled server error.</response>
    /// <returns>An <see cref="IActionResult" /> with delete result details.</returns>
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
                _ => StatusCode(500, new
                {
                    message = "Failed to delete voucher",
                    detail = errors[0].Description
                })
            });
    }

    /// <summary>
    ///     Soft deletes all expired vouchers.
    /// </summary>
    /// <param name="token">Cancellation token.</param>
    /// <response code="200">Expired vouchers deleted successfully.</response>
    /// <response code="401">Caller is not authenticated as Admin.</response>
    /// <response code="500">Failed to delete expired vouchers.</response>
    /// <returns>An <see cref="IActionResult" /> with bulk delete result details.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpDelete("expired")]
    public async Task<IActionResult> DeleteExpiredVouchers(CancellationToken token)
    {
        var result = await voucherService.DeleteVoucherExpireAsync(token);
        return result.Match<IActionResult>(
            _ => Ok(new { message = "Expired vouchers deleted successfully" }),
            errors => StatusCode(500, new
            {
                message = "Failed to delete expired vouchers",
                detail = errors[0].Description
            }));
    }

    /// <summary>
    ///     Retrieves vouchers for the admin dashboard with pagination.
    /// </summary>
    /// <param name="token">Cancellation token.</param>
    /// <param name="pageIndex">Page index (1-based).</param>
    /// <param name="pageSize">Number of items per page.</param>
    /// <response code="200">Vouchers retrieved successfully.</response>
    /// <response code="400">Request is invalid.</response>
    /// <response code="401">Caller is not authenticated as Admin.</response>
    /// <response code="500">Unhandled server error.</response>
    /// <returns>An <see cref="IActionResult" /> containing paginated voucher data.</returns>
    [ProducesResponseType(typeof(PageResult<ResponseVoucherAdminDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet]
    public async Task<IActionResult> GetVouchersForAdmin(CancellationToken token, [FromQuery] int pageIndex = 1,
        [FromQuery] int pageSize = 10)
    {
        var result = await voucherService.GetVoucherForAdminAsync(token, pageIndex, pageSize);
        return result.Match<IActionResult>(
            vouchers => Ok(vouchers),
            errors => BadRequest(new
            {
                message = "Failed to get vouchers for admin",
                detail = errors[0].Description
            }));
    }
}