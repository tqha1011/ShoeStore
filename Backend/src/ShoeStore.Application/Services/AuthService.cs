using System.Security.Cryptography;
using ErrorOr;
using Microsoft.Extensions.Caching.Memory;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using ShoeStore.Application.Constants;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.Notification;
using ShoeStore.Application.Interface.Strategies;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services;

public class AuthService(
    IPasswordHash passwordHash,
    IUserRepository userRepository,
    IUnitOfWork unitOfWork,
    ITokenService tokenService,
    IServiceProvider serviceProvider,
    IMemoryCache cache,
    IEmailService emailService,
    IConfiguration configuration) : IAuthService
{
    public async Task<ErrorOr<Created>> RegisterAsync(RegisterDto registerDto, CancellationToken token)
    {
        var email = registerDto.Email;
        var password = registerDto.Password;
        var res = await userRepository.IsEmailExistAsync(email, token);
        if (res) return Error.Conflict("Email.Exist", "Email already exists");

        var passHashed = passwordHash.HashPassword(password);
        var secureOtp = RandomNumberGenerator.GetInt32(100000, 1000000).ToString();
        var cachedUserPending = new VerifyOtpCachedDto(email, passHashed, secureOtp);
        cache.Set(CacheKey.GenerateOtpCacheKey(email), cachedUserPending, TimeSpan.FromMinutes(5));
        var emailBody = $"Here is your OTP: {secureOtp}. It will expired after 5 minutes";
        var senderEmail = configuration["Email:SenderName"] ??
                          throw new InvalidOperationException("Sender email configuration is missing");
        await emailService.SendEmailAsync(senderEmail, email, "Verify your email", emailBody, token);
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

    public async Task<ErrorOr<Created>> VerifyEmailWithOtpAsync(VerifyOtpRequestDto request, CancellationToken token)
    {
        var res = await userRepository.IsEmailExistAsync(request.Email, token);
        if (res) return Error.Conflict("Email.Exist", "Email already exists");
        var cacheKey = CacheKey.GenerateOtpCacheKey(request.Email);
        if (!cache.TryGetValue(cacheKey, out VerifyOtpCachedDto? cachedUserPending))
            return Error.NotFound("OTP.NotFound", "OTP not found or has expired");

        if (cachedUserPending?.OtpCode != request.OtpCode)
            return Error.Validation("OTP.Invalid", "Invalid OTP code");
        var newUser = new User
        {
            Email = cachedUserPending.Email,
            UserName = cachedUserPending.Email.Split('@')[0],
            CreatedAt = DateTime.UtcNow,
            Password = cachedUserPending.PasswordHash,
            Role = UserRole.User
        };
        userRepository.Add(newUser);
        await unitOfWork.SaveChangesAsync(token);
        return Result.Created;
    }
}