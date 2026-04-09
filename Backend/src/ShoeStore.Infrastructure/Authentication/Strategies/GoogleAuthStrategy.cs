using ErrorOr;
using Google.Apis.Auth;
using Microsoft.Extensions.Configuration;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface.Strategies;

namespace ShoeStore.Infrastructure.Authentication.Strategies;

/// <summary>
///     Apply strategy pattern to handle different social authentication providers.
///     This class implements the ISocialAuthStrategy interface for Google authentication.
/// </summary>
public class GoogleAuthStrategy(IConfiguration configuration) : ISocialAuthStrategy
{
    public async Task<ErrorOr<SocialUserDto>> VerifySocialToken(string accessToken, CancellationToken token = default)
    {
        var clientId = configuration["GoogleAuthentication:ClientId"];
        if (string.IsNullOrWhiteSpace(clientId))
            return Error.Unexpected("Google.MissingClientId", "Google Client ID is not configured.");
        try
        {
            var settings = new GoogleJsonWebSignature.ValidationSettings
            {
                Audience = [clientId]
            };

            var payload = await GoogleJsonWebSignature.ValidateAsync(accessToken, settings);
            if (payload == null)
                return Error.Unauthorized(
                    "Google.EmptyPayload",
                    "Token is valid but does not contain any user information.");
            return new SocialUserDto
            {
                Email = payload.Email,
                Username = payload.Name ?? payload.Email.Split('@')[0]
            };
        }
        catch (InvalidJwtException ex)
        {
            return Error.Unauthorized(
                "Google.InvalidToken",
                $"Token Google is invalid or expired: {ex.Message}");
        }
        catch (Exception ex)
        {
            return Error.Unexpected(
                "Google.VerificationFailed",
                $"Failed to verify token by Google: {ex.Message}");
        }
    }
}