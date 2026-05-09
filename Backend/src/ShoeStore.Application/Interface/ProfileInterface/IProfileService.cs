using ErrorOr;
using ShoeStore.Application.DTOs.ProfileDTOs;

namespace ShoeStore.Application.Interface.IProfileInterface
{
    public interface IProfileService
    {
        // PROFILE
        Task<ErrorOr<Updated>> UpdateProfile(Guid userGuid, UpdateProfileDto update, CancellationToken token);
        Task<ErrorOr<ResponseProfileDto>> GetProfile(Guid userGuid, CancellationToken token);
    }
}
