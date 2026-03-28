using ErrorOr;
using ShoeStore.Application.DTOs.AuthDTOs;

namespace ShoeStore.Application.Interface.Authentication;

public interface IAuthService
{
    public Task<ErrorOr<Created>> RegisterAsync(RegisterDto registerDto, CancellationToken token);

    public Task<ErrorOr<string>> LoginAsync(LoginDto loginDto, CancellationToken token);

    public Task<ErrorOr<string>> LoginWithSocialAsync(string providerName, string socialToken, CancellationToken token);
}