using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using ShoeStore.Application.DTOs.RestorePasswordDto;
using ShoeStore.Application.Interface.Authentication;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for handling password reset and recovery operations.
///     Provides a secure flow for users to verify their email, validate OTP codes, and update passwords.
///     All operations are rate-limited to prevent abuse and brute-force attacks.
/// </summary>
/// <param name="restorePasswordService">Service for handling password reset operations.</param>
[Route("api/[controller]")]
[ApiController]
public class RestorePassController(IRestorePasswordService restorePasswordService) : ControllerBase
{
    /// <summary>
    ///     Verifies that an email address exists in the system and sends an OTP code for password reset.
    /// </summary>
    /// <remarks>
    ///     Requires a request body with:
    ///     - <c>email</c>: the email address to verify (must exist in the system)
    ///     On successful verification, an OTP code is generated and sent to the email address.
    ///     The frontend should direct the user to enter the OTP they received via email.
    ///     Rate-limited per user to prevent email bombing attacks.
    /// </remarks>
    /// <param name="emailVerifyDto">DTO containing the email address to verify.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Email verified successfully. OTP code has been sent to the email address.</response>
    /// <response code="401">Unauthorized; email address not found in the system.</response>
    /// <response code="429">Too many requests; rate limit exceeded for this email address.</response>
    /// <response code="500">Internal server error; failed to send OTP email.</response>
    /// <returns>An action result containing a success message on success, or an error response describing what went wrong.</returns>
    [HttpPost("verify-email")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> VerifyEmail([FromBody] EmailVerifyDto emailVerifyDto, CancellationToken token)
    {
        var result = await restorePasswordService.SendRestorePasswordEmailAsync(emailVerifyDto.Email, token);

        var response = result.Match<IActionResult>(
            _ => Ok(new
            {
                message = "Email is valid. Please check your email for an OTP code."
            }),
            errors => errors[0].Code switch
            {
                "Email.Invalid" => Unauthorized(new
                {
                    message = "Email not found. Please check the email you entered.",
                    detail = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An error occurred while sending OTP. Please try again later.",
                    detail = errors[0].Description
                })
            });
        return response;
    }

    /// <summary>
    ///     Validates an OTP code sent to the user's email address for password reset verification.
    /// </summary>
    /// <remarks>
    ///     Requires a request body with:
    ///     - <c>email</c>: the email address associated with the password reset request
    ///     - <c>otp</c>: the one-time password code received via email (typically 6 digits)
    ///     This endpoint verifies the OTP matches what was sent to the email.
    ///     After successful verification, the user can proceed to update their password.
    ///     Rate-limited per user to prevent brute-force OTP guessing attacks.
    /// </remarks>
    /// <param name="otpVerifyDto">DTO containing email and OTP code to verify.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">OTP verified successfully. User can now proceed to update their password.</response>
    /// <response code="401">Unauthorized; OTP code is invalid or expired.</response>
    /// <response code="429">Too many requests; rate limit exceeded for this email address.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred during OTP verification.</response>
    /// <returns>An action result containing a success message on success, or an error response describing what went wrong.</returns>
    [HttpPost("verify-otp")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> VerifyOtp([FromBody] OtpVerifyDto otpVerifyDto, CancellationToken token)
    {
        var result = await restorePasswordService.VerifyOtpAsync(otpVerifyDto.Email, otpVerifyDto.Otp, token);

        var response = result.Match<IActionResult>(
            _ => Ok(new
            {
                message = "OTP is valid. You can now reset your password."
            }),
            errors => errors[0].Code switch
            {
                "OTP.Invalid" => Unauthorized(new
                {
                    message = "OTP is invalid. Please check the OTP code you entered.",
                    detail = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An error occurred while verifying OTP. Please try again later.",
                    detail = errors[0].Description
                })
            });
        return response;
    }

    /// <summary>
    ///     Updates the user's password after email and OTP verification.
    /// </summary>
    /// <remarks>
    ///     Requires a request body with:
    ///     - <c>email</c>: the email address associated with the account
    ///     - <c>otp</c>: the previously verified OTP code (must have been verified via verify-otp endpoint)
    ///     - <c>newPassword</c>: the new password for the account (must meet security requirements)
    ///     This is the final step in the password reset flow after email and OTP verification.
    ///     The OTP must be valid and match the email to prevent unauthorized password changes.
    ///     Rate-limited per user to prevent abuse.
    /// </remarks>
    /// <param name="updatePasswordDto">DTO containing email, OTP, and new password.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Password updated successfully. User can now log in with the new password.</response>
    /// <response code="401">Unauthorized; OTP is invalid or expired.</response>
    /// <response code="429">Too many requests; rate limit exceeded for this email address.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred during password update.</response>
    /// <returns>An action result containing a success message on success, or an error response describing what went wrong.</returns>
    [HttpPost("update-password")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> UpdatePassword([FromBody] UpdatePasswordDto updatePasswordDto,
        CancellationToken token)
    {
        var result = await restorePasswordService.UpdatePasswordAsync(updatePasswordDto.Email, updatePasswordDto.Otp,
            updatePasswordDto.NewPassword, token);

        var response = result.Match<IActionResult>(
            _ => Ok(new
            {
                message = "Password updated successfully. You can now log in with your new password."
            }),
            errors => errors[0].Code switch
            {
                "OTP.Invalid" => Unauthorized(new
                {
                    message = "OTP is invalid. Please check the OTP code you entered.",
                    detail = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An error occurred while updating password. Please try again later.",
                    detail = errors[0].Description
                })
            });
        return response;
    }
}