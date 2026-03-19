using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface;

namespace ShoeStore.Api.Controllers;
/// <summary>
/// Controller for handling user authentication and registration related operations, such as signing in and signing up.
/// </summary>
/// <param name="authService"></param>
[Route("api/[controller]")]
[ApiController]
public class AuthController(IAuthService authService) : ControllerBase
{
    /// <summary>
    /// Authenticate user and return JWT token if successful, otherwise return error message
    /// </summary>
    /// <param name="loginDto"></param>
    /// <param name="token"></param>
    /// <returns>
    /// JWT token if login is successful, otherwise return error message with description of the failure reason
    /// <response code="200"> Login successfully and return JWT Token </response>
    /// <response code="400"> Login failed </response>
    /// </returns>
    [HttpPost("signin")]
    public async Task<IActionResult> Signin([FromBody] LoginDto loginDto,CancellationToken token)
    {
        var result = await authService.LoginAsync(loginDto, token);
        
        // Use pattern matching to handle the result
        var response = result.Match<IActionResult>(
            jwtToken => Ok(jwtToken),
            errors => BadRequest(new
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
    /// <response code="400"> Sign up failed </response>
    /// </returns>
    [HttpPost("signup")]
    public async Task<IActionResult> Signup([FromBody] RegisterDto registerDto, CancellationToken token)
    {
        var result = await authService.RegisterAsync(registerDto, token);

        var response = result.Match<IActionResult>(
            _ => Ok(new
            {
                message = "Sign up successfully",
            }),
            errors => BadRequest(new
            {
                message = "Sign up failed",
                description = errors[0].Description
            }));
        return response;
    }
}