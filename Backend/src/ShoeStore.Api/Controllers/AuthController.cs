using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface.Authentication;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for handling user authentication and registration related operations, such as signing in and signing up.
///     Apply rate-limit to protect API
/// </summary>
/// <param name="authService"></param>
[Route("api/auth")]
[ApiController]
public class AuthController(IAuthService authService) : ControllerBase
{
    /// <summary>
    ///     Authenticates a user with email and password credentials.
    /// </summary>
    /// <remarks>
    ///     Requires a request body with:
    ///     - <c>email</c>: the user's email address
    ///     - <c>password</c>: the user's password
    ///     Validates credentials and returns a JWT token if authentication succeeds.
    ///     Rate-limited per user to prevent brute-force attacks.
    /// </remarks>
    /// <param name="loginDto">The login credentials (email and password).</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Login successful; returns JWT token.</response>
    /// <response code="401">Unauthorized; email or password is incorrect.</response>
    /// <returns>An action result containing the JWT token on success, or an error message on failure.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [HttpPost("signin")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> Signin([FromBody] LoginDto loginDto, CancellationToken token)
    {
        var result = await authService.LoginAsync(loginDto, token);

        // Use pattern matching to handle the result
        var response = result.Match<IActionResult>(
            jwtToken => Ok(new
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
    ///     Registers a new user with email and password.
    /// </summary>
    /// <remarks>
    ///     Requires a request body with:
    ///     - <c>email</c>: the user's email address (must be unique)
    ///     - <c>password</c>: the user's password
    ///     Creates a new user account if the email is not already registered.
    ///     Rate-limited per user to prevent abuse.
    /// </remarks>
    /// <param name="registerDto">The registration details (email and password).</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">User registered successfully.</response>
    /// <response code="400">Bad request; invalid registration information provided.</response>
    /// <response code="409">Conflict; email address already exists.</response>
    /// <returns>An action result containing a success message on success, or an error message on failure.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status409Conflict)]
    [HttpPost("signup")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> Signup([FromBody] RegisterDto registerDto, CancellationToken token)
    {
        var result = await authService.RegisterAsync(registerDto, token);

        var response = result.Match<IActionResult>(
            _ => Ok(new
            {
                message = "Sign up successfully"
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
    ///     Authenticates a user with a Google account.
    /// </summary>
    /// <remarks>
    ///     Requires a request body with:
    ///     - <c>idToken</c>: the ID token obtained from Google OAuth 2.0
    ///     Validates the token and returns a JWT token if authentication succeeds.
    ///     Rate-limited per user to prevent abuse.
    /// </remarks>
    /// <param name="googleLoginDto">The Google login details containing the ID token.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Login successful; returns JWT token.</response>
    /// <response code="400">Bad request; invalid Google token or missing required data.</response>
    /// <response code="401">Unauthorized; Google token is invalid or expired.</response>
    /// <response code="500">Internal server error; failed to process Google login.</response>
    /// <returns>An action result containing the JWT token on success, or an error message on failure.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost("signin-google")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> SigninWithGoogle([FromBody] GoogleLoginDto googleLoginDto, CancellationToken token)
    {
        var idToken = googleLoginDto.IdToken;
        var result = await authService.LoginWithSocialAsync("Google", idToken, token);

        var response = result.Match<IActionResult>(
            jwtToken => Ok(new
            {
                token = jwtToken
            }),
            errors =>
            {
                // Get the first error to determine the exact failure reason
                var firstError = errors[0];

                return firstError.Code switch
                {
                    // Handle 401 Unauthorized errors (Client sent a bad or expired token)
                    "Google.InvalidToken" or "Google.EmptyPayload" => Unauthorized(new
                    {
                        message = "Invalid Google token",
                        detail = firstError.Description
                    }),

                    // Handle 500 Internal Server Errors (Server configuration or Google API down)
                    "Google.MissingClientId" or "Google.VerificationFailed" => StatusCode(
                        StatusCodes.Status500InternalServerError, new
                        {
                            message = "An error occurred while processing Google login",
                            detail = firstError.Description
                        }),

                    // Fallback for any other unhandled errors -> 400 Bad Request
                    _ => BadRequest(new
                    {
                        message = "Failed to login with Google",
                        detail = firstError.Description
                    })
                };
            });

        return response;
    }

    /// <summary>
    ///     Authenticates a user with a Facebook account.
    /// </summary>
    /// <remarks>
    ///     Requires a request body with:
    ///     - <c>accessToken</c>: the access token obtained from Facebook OAuth 2.0
    ///     The user must grant email permission for successful authentication.
    ///     Validates the token and returns a JWT token if authentication succeeds.
    ///     Rate-limited per user to prevent abuse.
    /// </remarks>
    /// <param name="facebookAuthDto">The Facebook login details containing the access token.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Login successful; returns JWT token.</response>
    /// <response code="400">Bad request; missing email permission or invalid access token.</response>
    /// <response code="401">Unauthorized; Facebook access token is invalid or expired.</response>
    /// <response code="500">Internal server error; failed to process Facebook login.</response>
    /// <returns>An action result containing the JWT token on success, or an error message on failure.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost("signin-facebook")]
    [EnableRateLimiting("limit-per-user")]
    public async Task<IActionResult> SigninWithFacebook([FromBody] FacebookAuthDto facebookAuthDto,
        CancellationToken token)
    {
        var accessToken = facebookAuthDto.AccessToken;
        var result = await authService.LoginWithSocialAsync("Facebook", accessToken, token);

        var response = result.Match<IActionResult>(
            jwtToken => Ok(jwtToken),
            errors =>
            {
                // Get the first error to determine the exact failure reason
                var firstError = errors[0];

                return firstError.Code switch
                {
                    // Handle 401 Unauthorized (Invalid or expired access token)
                    "Facebook.InvalidToken" => Unauthorized(new
                    {
                        message = "Invalid Facebook access token",
                        detail = firstError.Description
                    }),

                    // Handle 400 Bad Request (Validation issue: User declined email permission)
                    "Facebook.EmailMissing" => BadRequest(new
                    {
                        message = "Email access permission is required",
                        detail = firstError.Description
                    }),

                    // Handle 500 Internal Server Errors (Missing configuration or Graph API connection failure)
                    "Facebook.MissingUrl" or "Facebook.Exception" => StatusCode(
                        StatusCodes.Status500InternalServerError, new
                        {
                            message = "An error occurred while processing Facebook login",
                            detail = firstError.Description
                        }),

                    // Fallback for any other unhandled errors -> 400 Bad Request
                    _ => BadRequest(new
                    {
                        message = "Failed to login with Facebook",
                        detail = firstError.Description
                    })
                };
            });

        return response;
    }
}