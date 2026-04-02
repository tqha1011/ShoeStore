using ErrorOr;
using Microsoft.Extensions.DependencyInjection;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.Strategies;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services;

public class AuthService(
    IPasswordHash passwordHash,
    IUserRepository userRepository,
    IUnitOfWork unitOfWork,
    ITokenService tokenService,
    IServiceProvider serviceProvider) : IAuthService
{
    public async Task<ErrorOr<Created>> RegisterAsync(RegisterDto registerDto, CancellationToken token)
    {
        var email = registerDto.Email;
        var password = registerDto.Password;
        var res = await userRepository.IsEmailExistAsync(email, token);
        if (res) return Error.Conflict("Email.Exist", "Email already exists");

        var passHashed = passwordHash.HashPassword(password);
        var user = new User
        {
            Email = email,
            Password = passHashed,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow,
            UserName = email.Split('@')[0],
            Role = UserRole.User
        };
        userRepository.Add(user);
        await unitOfWork.SaveChangesAsync(token);
        return Result.Created;
    }

    public async Task<ErrorOr<string>> LoginAsync(LoginDto loginDto, CancellationToken token)
    {
        var correctUser = await userRepository.GetUserByEmailAsync(loginDto.Email, token);

        if (correctUser == null || !passwordHash.VerifyPassword(loginDto.Password, correctUser.Password))
            return Error.Unauthorized("Invalid.Credential", "Email or password is incorrect");

        // Generate JWT token
        var jwtToken = tokenService.GenerateToken(correctUser.PublicId, correctUser.Email, correctUser.Role);
        return jwtToken;
    }

    /// <summary>
    ///     Apply strategy pattern to support multiple social login providers, such as Google, Facebook, etc.
    /// </summary>
    /// <param name="providerName"></param>
    /// <param name="accessToken"></param>
    /// <param name="token"></param>
    /// <returns></returns>
    public async Task<ErrorOr<string>> LoginWithSocialAsync(string providerName, string accessToken,
        CancellationToken token)
    {
        var authStrategy = serviceProvider.GetRequiredKeyedService<ISocialAuthStrategy>(providerName);

        var response = await authStrategy.VerifySocialToken(accessToken, token);

        if (response.IsError) return response.Errors;

        var result = await userRepository.GetUserByEmailAsync(response.Value.Email, token);
        if (result == null)
        {
            var newUser = new User
            {
                Email = response.Value.Email,
                UserName = response.Value.Username ?? response.Value.Email.Split('@')[0],
                CreatedAt = DateTime.UtcNow,
                Password = passwordHash.HashPassword(Guid.NewGuid()
                    .ToString()), // Generate a random password for social login user, since they won't use it to login
                Role = UserRole.User
            };
            userRepository.Add(newUser);
            await unitOfWork.SaveChangesAsync(token);
            var jwtToken = tokenService.GenerateToken(newUser.PublicId, newUser.Email, newUser.Role);
            return jwtToken;
        }
        else
        {
            var jwtToken = tokenService.GenerateToken(result.PublicId, result.Email, result.Role);
            return jwtToken;
        }
    }
}