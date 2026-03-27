using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using ShoeStore.Application.DTOs.RestorePasswordDto;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for Restore Password service
/// </summary>
[Route("api/[controller]")]
[ApiController]
public class RestorePassController(IRestorePasswordService restorePasswordService) : ControllerBase
{
    /// <summary>
    ///     API verify email, Email is required
    /// </summary>
    /// <param name="emailVerifyDto"></param>
    /// <param name="token"></param>
    /// <returns>
    ///     <response code="200"> Email is valid. Please check your email for an OTP code. </response>
    ///     <response code="401"> Email not found. Please check the email you entered. </response>
    ///     <response code="500"> An error occurred while sending OTP. Please try again later</response>
    /// </returns>
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
    ///     API to verify OTP code, OTP code is required, email is required
    /// </summary>
    /// <param name="otpVerifyDto"></param>
    /// <param name="token"></param>
    /// <returns>
    ///     <response code="200">OTP is valid.You can now reset your password</response>
    ///     <response code="500">An error occurred while verifying OTP. Please try again later. </response>
    /// </returns>
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
    ///     API to update password, (Email,Otp,new password) is required
    /// </summary>
    /// <param name="updatePasswordDto"></param>
    /// <param name="token"></param>
    /// <returns>
    ///     <response code="200">Password updated successfully. You can now log in with your new password.</response>`
    ///     <response code="401">OTP is invalid. Please check the OTP code you entered.</response>
    ///     <response code="500">An error occurred while updating password. Please try again later.</response>
    /// </returns>
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