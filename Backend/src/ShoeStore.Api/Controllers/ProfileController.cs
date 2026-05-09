using Asp.Versioning;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ProfileDTOs;
using ShoeStore.Application.Interface.ProfileInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
/// Controller for managing user profile information.
/// </summary>
/// <param name="profileService">The profile service instance.</param>
//[Authorize]
[Route("api/profile")]
[ApiController]
public class ProfileController(IProfileService profileService) : ControllerBase
{
    /// <summary>
    /// Retrieves the profile information for a specific user.
    /// </summary>
    /// <param name="userGuid">The unique identifier of the user.</param>
    /// <param name="token">Cancellation token.</param>
    /// <response code="200">Returns the user profile.</response>
    /// <response code="404">User not found.</response>
    /// <returns>The user profile data.</returns>
    [HttpGet("{userGuid}")]
    [ProducesResponseType(typeof(ResponseProfileDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetProfile(Guid userGuid, CancellationToken token)
    {
        var result = await profileService.GetProfileAsync(userGuid, token);

        return result.Match<IActionResult>(
            profile => Ok(profile),
            errors => errors[0].Code switch
            {
                "User.NotFound" => NotFound(new { message = "User not found", detail = errors[0].Description }),
                _ => BadRequest(new { message = "An error occurred", detail = errors[0].Description })
            }
        );
    }

    /// <summary>
    /// Updates the profile information for a specific user.
    /// </summary>
    /// <param name="userGuid">The unique identifier of the user.</param>
    /// <param name="updateProfileDto">The updated profile data.</param>
    /// <param name="token">Cancellation token.</param>
    /// <response code="200">Profile updated successfully.</response>
    /// <response code="400">Invalid input data.</response>
    /// <response code="404">User not found.</response>
    /// <returns>A status indicating the result of the update.</returns>
    [HttpPut("{userGuid}")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> UpdateProfile(Guid userGuid, [FromBody] UpdateProfileDto updateProfileDto, CancellationToken token)
    {
        var result = await profileService.UpdateProfileAsync(userGuid, updateProfileDto, token);

        return result.Match<IActionResult>(
            _ => Ok(new { message = "Profile updated successfully" }),
            errors => errors[0].Code switch
            {
                "User.NotFound" => NotFound(new { message = "User not found", detail = errors[0].Description }),
                _ => BadRequest(new { message = "Failed to update profile", detail = errors[0].Description })
            }
        );
    }
    /// <summary>
    /// Changes the password for a specific user.
    /// </summary>
    /// <param name="userGuid">
    /// The unique identifier of the user.
    /// </param>
    /// <param name="changePasswordDto">
    /// The password change request data.
    /// </param>
    /// <param name="token">
    /// Cancellation token.
    /// </param>
    /// <response code="200">
    /// Password changed successfully.
    /// </response>
    /// <response code="400">
    /// Invalid password data.
    /// </response>
    /// <response code="404">
    /// User not found.
    /// </response>
    /// <returns>
    /// A status indicating the result of the password change.
    /// </returns>
    [HttpPut("{userGuid}/change-password")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> ChangePassword(Guid userGuid, [FromBody] ChangePasswordDto changePasswordDto, CancellationToken token)
    {
        var result = await profileService.ChangePasswordAsync(userGuid, changePasswordDto, token);

        return result.Match<IActionResult>(
            _ => Ok(new
            {
                message = "Password changed successfully"
            }),

            errors => errors[0].Code switch
            {
                "User.NotFound" => NotFound(new
                {
                    message = "User not found",
                    detail = errors[0].Description
                }),

                "Password.InvalidCurrentPassword" => BadRequest(new
                {
                    message = "Current password is incorrect",
                    detail = errors[0].Description
                }),

                "Password.ConfirmMismatch" => BadRequest(new
                {
                    message = "Confirm password does not match",
                    detail = errors[0].Description
                }),

                _ => BadRequest(new
                {
                    message = "Failed to change password",
                    detail = errors[0].Description
                })
            }
        );
    }
}

