using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.VoucherDtos;
using ShoeStore.Application.Interface.VoucherInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for managing user-specific voucher operations.
///     Provides endpoints for users to retrieve their own vouchers.
/// </summary>
/// <param name="userVoucherService">Service for handling user-voucher relationship operations.</param>
[ApiController]
[Route("api/user/vouchers")]
[Authorize(Roles = "User")]
public class UserVoucherController(IUserVoucherService userVoucherService) : ControllerBase
{
    /// <summary>
    ///     Retrieves all active vouchers associated with a specific user.
    /// </summary>
    /// <remarks>
    ///     Requires User role authorization.
    ///     Returns a list of vouchers that are currently valid and assigned to the user.
    /// </remarks>
    /// <param name="userGuid">The unique identifier (GUID) of the user whose vouchers are being retrieved.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Vouchers retrieved successfully.</response>
    /// <response code="401">Unauthorized; user must be authenticated with User role.</response>
    /// <response code="404">Not found; no vouchers found for the specified user.</response>
    /// <response code="500">Internal server error; an unexpected error occurred.</response>
    /// <returns>An action result containing a paginated list of user vouchers on success, or an error response.</returns>
    [ProducesResponseType(typeof(PageResult<ResponseVoucherUserDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("user/{userGuid}")]
    public async Task<IActionResult> GetVouchersForUser(Guid userGuid, CancellationToken token)
    {
        var result = await userVoucherService.GetAllVoucherForUserAsync(userGuid, token);
        return result.Match<IActionResult>(
            vouchers => Ok(vouchers),
            errors => errors[0].Code switch
            {
                "NO_VOUCHERS_FOUND" => NotFound(new
                {
                    message = "No vouchers found for this user",
                    detail = errors[0].Description
                }),
                _ => BadRequest(new
                {
                    message = "Failed to retrieve vouchers for user",
                    detail = errors[0].Description
                })
            });
    }
}
