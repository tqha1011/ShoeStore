using ShoeStore.Application.Interface;
using ErrorOr;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;
using Google.Apis.Auth;
using Microsoft.Extensions.Configuration;

namespace ShoeStore.Infrastructure.Authentication;

public class AuthService(
    IPasswordHash passwordHash,
    IUserRepository userRepository,
    IUnitOfWork unitOfWork,
    ITokenService tokenService,
    IConfiguration configuration) : IAuthService
{
    public async Task<ErrorOr<Created>> RegisterAsync(RegisterDto registerDto, CancellationToken token)
    {
        var email = registerDto.Email;
        var password = registerDto.Password;
        var res = await userRepository.IsEmailExistAsync(email, token);
        if (res)
        {
            return Error.Failure("Email.Exist","Email already exists");
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
        var jwtToken = tokenService.GenerateToken(correctUser.Id, correctUser.Email, correctUser.Role);
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
                    Password = string.Empty // No password for Google sign-in users
                };
                userRepository.Add(newUser);
                await unitOfWork.SaveChangesAsync(token);
                var jwtToken = tokenService.GenerateToken(newUser.Id, newUser.Email, newUser.Role);
                return jwtToken;
            }
            else
            {
                var jwtToken = tokenService.GenerateToken(correctUser.Id, correctUser.Email, correctUser.Role);
                return jwtToken;
            }
        }
        catch (Exception e)
        {
            return Error.Failure("Google.Exception",$"Exception occured: {e.Message}");
        }
    }
}