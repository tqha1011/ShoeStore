using ErrorOr;
using ShoeStore.Application.DTOs.ProfileDTOs;

namespace ShoeStore.Application.Interface.ProfileInterface
{
    public interface IProfileService
    {
        // PROFILE
        Task<ErrorOr<Updated>> UpdateProfileAsync(Guid userGuid, UpdateProfileDto update, CancellationToken token);
        Task<ErrorOr<ResponseProfileDto>> GetProfileAsync(Guid userGuid, CancellationToken token);
        // PASSWORD
        Task<ErrorOr<Updated>> ChangePasswordAsync(Guid userGuid, ChangePasswordDto changePassWord, CancellationToken token);
    }
}
