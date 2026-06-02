using ShoeStore.Application.DTOs.AuthDTOs;

namespace ShoeStore.Application.Interface.Strategies;

public interface IGoogleValidator
{
    Task<GooglePayloadDto?> ValidateTokenAsync(string clientId, string idToken, CancellationToken token);
}