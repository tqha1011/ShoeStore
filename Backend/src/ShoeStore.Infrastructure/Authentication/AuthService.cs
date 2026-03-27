using System.Text.Json;
using ShoeStore.Application.Interface;
using ErrorOr;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;
using Google.Apis.Auth;
using Microsoft.Extensions.Configuration;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.Common;

namespace ShoeStore.Infrastructure.Authentication;

public class AuthService(
    IPasswordHash passwordHash,
    IUserRepository userRepository,
    IUnitOfWork unitOfWork,
    ITokenService tokenService,
    IConfiguration configuration,
    IHttpClientFactory httpClientFactory) : IAuthService
{
    public async Task<ErrorOr<Created>> RegisterAsync(RegisterDto registerDto, CancellationToken token)
    {
        var email = registerDto.Email;
        var password = registerDto.Password;
        var res = await userRepository.IsEmailExistAsync(email, token);
        if (res)
        {
            return Error.Conflict("Email.Exist","Email already exists");
        }
        
        var passHashed = passwordHash.HashPassword(password);
        var user = new User
        {
            Email = email,
            Password = passHashed,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow,
            UserName = email.Split('@')[0],
            Role = UserRole.Customer
        };
        userRepository.Add(user);
        await unitOfWork.SaveChangesAsync(token);
        return Result.Created;
    }

    public async Task<ErrorOr<string>> LoginAsync(LoginDto loginDto, CancellationToken token)
    {
        var correctUser = await userRepository.GetUserByEmailAsync(loginDto.Email, token);
        
        if (correctUser == null || !passwordHash.VerifyPassword(loginDto.Password,correctUser.Password))
        {
            return Error.Unauthorized("Invalid.Credential", "Email or password is incorrect");
        }
        
        // Generate JWT token
        var jwtToken = tokenService.GenerateToken(correctUser.PublicId, correctUser.Email, correctUser.Role);
        return jwtToken;
    }

    public async Task<ErrorOr<string>> LoginWithGoogleAsync(string idToken, CancellationToken token)
    {
        try
        {
            var payload = await GoogleJsonWebSignature.ValidateAsync(idToken,
                new GoogleJsonWebSignature.ValidationSettings
                {
                    Audience = 
                    [
                        configuration["GOOGLEAUTHENTICATION_CLIENTID"] 
                        ?? 
                        throw new InvalidOperationException("GOOGLEAUTHENTICATION_CLIENTID is missing")
                    ]
                });
            var email = payload.Email;
            var correctUser = await userRepository.GetUserByEmailAsync(email, token);
            if (correctUser == null)
            {
                var newUser = new User
                {
                    Email = email,
                    UserName = email.Split('@')[0],
                    Role = UserRole.Customer,
                    CreatedAt = DateTime.UtcNow,
                    UpdatedAt = DateTime.UtcNow,
                    Password = passwordHash.HashPassword(Guid.NewGuid().ToString()) // Generate a random password for Google sign-in users, since they won't use it to log in
                };
                userRepository.Add(newUser);
                await unitOfWork.SaveChangesAsync(token);
                var jwtToken = tokenService.GenerateToken(newUser.PublicId, newUser.Email, newUser.Role);
                return jwtToken;
            }
            else
            {
                var jwtToken = tokenService.GenerateToken(correctUser.PublicId, correctUser.Email, correctUser.Role);
                return jwtToken;
            }
        }
        catch (Exception e)
        {
            return Error.Failure("Google.Exception",$"Exception occured: {e.Message}");
        }
    }

    public async Task<ErrorOr<string>> LoginWithFacebookAsync(string accessToken, CancellationToken token)
    {
        try
        {
            var url = configuration["FACEBOOKAUTHENTICATION_URL"] 
                      ?? throw new InvalidOperationException("Facebook.Url is missing");
            var graphApi = $"{url}me?fields=id,name,email&access_token={accessToken}";
            
            var response = await httpClientFactory.CreateClient().GetAsync(graphApi,token);
            if (!response.IsSuccessStatusCode)
            {
                return Error.Unauthorized("Facebook.InvalidToken", "Invalid Facebook access token");
            }
            // deserialize into the FacebookLoginDto to get the email
            var json = await JsonSerializer.DeserializeAsync<FacebookLoginDto>(
                await response.Content.ReadAsStreamAsync(token),cancellationToken: token);
            
            // validate if user don't give permission to access email, if so, return error message to ask for email access permission
            if(json == null || string.IsNullOrEmpty(json.Email))
            {
                return Error.Validation("Facebook.EmailMissing","This app needs permission to access your email address. Please allow email access and try again.");
            }
            
            var correctUser = await userRepository.GetUserByEmailAsync(json.Email, token);
            if (correctUser == null)
            {
                var newUser = new User
                {
                    Email = json.Email,
                    UserName = json.Name,
                    Role = UserRole.Customer,
                    CreatedAt = DateTime.UtcNow,
                    UpdatedAt = DateTime.UtcNow,
                    Password = passwordHash.HashPassword(Guid.NewGuid().ToString())
                };
                userRepository.Add(newUser);
                await unitOfWork.SaveChangesAsync(token);
                var jwtToken = tokenService.GenerateToken(newUser.PublicId, newUser.Email, newUser.Role);
                return jwtToken;
            }
            else
            {
                var jwtToken = tokenService.GenerateToken(correctUser.PublicId, correctUser.Email, correctUser.Role);
                return jwtToken;
            }
        }
        catch (Exception e)
        {
            return Error.Failure("Facebook.Exception",$"Exception occured: {e.Message}");
        }
    }
}