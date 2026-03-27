using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface;

namespace ShoeStore.Api.Controllers;
/// <summary>
/// Controller for handling user authentication and registration related operations, such as signing in and signing up.
/// Apply rate-limit to protect API
/// </summary>
/// <param name="authService"></param>
[Route("api/auth")]
[ApiController]
public class AuthController(IAuthService authService) : ControllerBase
{
    /// <summary>
    /// Sign in user
    /// Authenticate user credentials and return JWT token if login is successful, otherwise return error message with description of the failure reason
    /// </summary>
    /// <param name="loginDto"></param>
    /// <param name="token"></param>
    /// <returns>
    /// JWT token if login is successful, otherwise return error message with description of the failure reason
    /// <response code="200"> Login successfully and return JWT Token </response>
    /// <response code="401"> Unauthorized if pass/email is incorrect </response>
    /// </returns>
    [HttpPost("signin")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> Signin([FromBody] LoginDto loginDto,CancellationToken token)
    {
        var result = await authService.LoginAsync(loginDto, token);
        
        // Use pattern matching to handle the result
        var response = result.Match<IActionResult>(
            jwtToken => Ok( new
            {
                token = jwtToken
            }),
            errors => Unauthorized(new
            {
                message = "Failed to login",
                description = errors[0].Description
            }));
        return response;
    }
    
    /// <summary>
    /// Register new user 
    /// </summary>
    /// <param name="registerDto"></param>
    /// <param name="token"></param>
    /// <returns>
    /// Success message if registration is successful, otherwise return error message with description of the failure reason
    /// <response code="200"> Sign up successfully </response>
    /// <response code="409"> Email already exist </response>
    /// <respone code="400"> Failed to sign up due to invalid information provided by user </respone>
    /// </returns>
    [HttpPost("signup")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> Signup([FromBody] RegisterDto registerDto, CancellationToken token)
    {
        var result = await authService.RegisterAsync(registerDto, token);

        var response = result.Match<IActionResult>(
            _ => Ok(new
            {
                message = "Sign up successfully",
            }),
            errors => errors[0].Code switch
            {
                "Email.Exist" => Conflict(new
                {
                    message = "Email already exist",
                    detail = errors[0].Description
                }),
                _ => BadRequest(new
                {
                    message = "Failed to sign up",
                    detail = errors[0].Description
                })
            });
        return response;
    }

    /// <summary>
    /// Sign in user with Google account
    /// API requires Frontend send a idToken taken from Google
    /// </summary>
    /// <param name="googleLoginDto"></param>
    /// <param name="token"></param>
    /// <returns>
    /// <response code="200"> Login successfully and return JWT Token </response>
    /// <response code="400"> Failed to log in with Google due to invalid information provided by user </response>
    /// <response code="500"> An error occurred while processing Google login </response>
    /// </returns>
    [HttpPost("signin-google")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> SigninWithGoogle([FromBody] GoogleLoginDto googleLoginDto, CancellationToken token)
    {
        var idToken = googleLoginDto.IdToken;
        var result = await authService.LoginWithGoogleAsync(idToken, token);

        var response = result.Match<IActionResult>(
            jwtToken => Ok(new
            {
                token = jwtToken
            }),
            errors => errors[0].Code switch
            {
                "Google.Exception" => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An error occurred while processing Google login",
                    detail = errors[0].Description
                }),
                _ => BadRequest(new
                {
                    message = "Failed to login with Google",
                    detail = errors[0].Description
                })
            });
        return response;
    }

    /// <summary>
    /// API required FE send a accessToken
    /// </summary>
    /// <param name="facebookAuthDto"></param>
    /// <param name="token"></param>
    /// <returns>
    /// <response code="200"> Login successfully and return JWT Token </response>
    /// <response code="400"> Failed to log in with Facebook due to invalid information provided by user </response>
    /// <response code="401"> Unauthorized if Facebook access token is invalid </response>
    /// <response code="500"> An error occurred while processing Facebook login </response>
    /// </returns>
    [HttpPost("signin-facebook")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> SigninWithFacebook([FromBody] FacebookAuthDto facebookAuthDto,
        CancellationToken token)
    {
        var accessToken = facebookAuthDto.AccessToken;
        var result = await authService.LoginWithFacebookAsync(accessToken, token);

        var response = result.Match<IActionResult>(
            jwtToken => Ok(new
            {
                token = jwtToken
            }),
            errors => errors[0].Code switch
            {
                "Facebook.InvalidToken" => Unauthorized(new
                {
                    message = "Invalid Facebook access token",
                    detail = errors[0].Description
                }),
                "Facebook.Exception" => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An error occurred while processing Facebook login",
                    detail = errors[0].Description
                }),
                "Facebook.EmailMissing" => BadRequest(new
                {
                    message = "Email access permission is required",
                    detail = errors[0].Description
                }),
                _ => BadRequest(new
                {
                    message = "Failed to login with Facebook",
                    detail = errors[0].Description
                })
            });
        return response;
    }
}