using ErrorOr;
using ShoeStore.Application.DTOs.AuthDTOs;

namespace ShoeStore.Application.Interface;

public interface IAuthService
{
    public Task<ErrorOr<Created>> RegisterAsync(RegisterDto registerDto,CancellationToken token);
    
    public Task<ErrorOr<string>> LoginAsync(LoginDto loginDto, CancellationToken token);

    public Task<ErrorOr<string>> LoginWithGoogleAsync(string idToken,CancellationToken token);
    
    public Task<ErrorOr<string>> LoginWithFacebookAsync(string accessToken, CancellationToken token);
}