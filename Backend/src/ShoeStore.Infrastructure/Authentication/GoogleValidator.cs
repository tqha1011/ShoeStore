using Google.Apis.Auth;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Application.Interface.Strategies;

namespace ShoeStore.Infrastructure.Authentication;

public class GoogleValidator : IGoogleValidator
{
    public async Task<GooglePayloadDto?> ValidateTokenAsync(string clientId, string idToken, CancellationToken token)
    {
        var settings = new GoogleJsonWebSignature.ValidationSettings
        {
            Audience = [clientId]
        };

        var payload = await GoogleJsonWebSignature.ValidateAsync(idToken, settings);
        if (payload == null)
            return null;
        return new GooglePayloadDto(
            payload.Email,
            payload.Name,
            payload.Subject,
            payload.Picture);
    }
}