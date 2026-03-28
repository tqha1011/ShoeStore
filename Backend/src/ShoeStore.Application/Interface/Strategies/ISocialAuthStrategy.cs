using ShoeStore.Application.DTOs.AuthDTOs;
using ErrorOr;

namespace ShoeStore.Application.Interface.Strategies;

public interface ISocialAuthStrategy
{
    public Task<ErrorOr<SocialUserDto>> VerifySocialToken(string accessToken, CancellationToken token = default);
}