using ErrorOr;
using ShoeStore.Application.DTOs.ProfileDTOs;
using ShoeStore.Application.Interface.ProfileInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.Common;
using BCrypt.Net;

namespace ShoeStore.Application.Services
{
    public class ProfileService : IProfileService
    {
        private readonly IUserRepository _userRepository;
        private readonly IUnitOfWork _uow;

        public ProfileService(IUserRepository userRepository, IUnitOfWork uow)
        {
            _userRepository = userRepository;
            _uow = uow;
        }

        public async Task<ErrorOr<Updated>> ChangePasswordAsync(Guid userGuid, ChangePasswordDto changePassword, CancellationToken token)
        {
            var user = await _userRepository.GetUserByPublicIdAsync(userGuid, token);
            if(user == null)
                return Error.NotFound("User.NotFound", $"User with ID {userGuid} was not found.");

            var isCorrectPassword = BCrypt.Net.BCrypt.Verify(
                changePassword.OldPassword,
                user.Password);

            if (!isCorrectPassword)
            {
                return Error.Validation(
                    "Password.InvalidCurrentPassword",
                    "Current password is incorrect");
            }
        }

        public async Task<ErrorOr<ResponseProfileDto>> GetProfileAsync(Guid userGuid, CancellationToken token)
        {
            var profile = await _userRepository.GetUserByPublicIdAsync(userGuid, token);

            if(profile == null)
                return Error.NotFound("User.NotFound", $"User with ID {userGuid} was not found.");

            return new ResponseProfileDto
            {
                UserGuid = userGuid,
                UserName = profile.UserName,
                Password = profile.Password,
                Email = profile.Email,
                DateOfBirth = profile.DateOfBirth,
            };
        }

        public async Task<ErrorOr<Updated>> UpdateProfileAsync(Guid userGuid, UpdateProfileDto update, CancellationToken token)
        {
            var profile = await _userRepository.GetUserByPublicIdAsync(userGuid, token);
            if (profile == null)
                return Error.NotFound("User.NotFound", $"User with ID {userGuid} was not found.");

            profile.UserName = update.UserName;
            profile.DateOfBirth = update.DateOfBirth;
            // Avata Url
            profile.UpdatedAt = DateTime.Now;

            _userRepository.Update(profile);
            await _uow.SaveChangesAsync(token);
            return Result.Updated;
        }
    }
}
