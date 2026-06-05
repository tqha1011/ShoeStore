using ErrorOr;
using ShoeStore.Application.DTOs.ProfileDTOs;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProfileInterface;
using ShoeStore.Application.Interface.UserInterface;

namespace ShoeStore.Application.Services;

public class ProfileService(
    IUserRepository userRepository,
    IUnitOfWork uow,
    IPasswordHash passwordHash) : IProfileService
{
    public async Task<ErrorOr<Updated>> ChangePasswordAsync(Guid userGuid, ChangePasswordDto changePassword,
        CancellationToken token)
    {
        var user = await userRepository.GetUserByPublicIdAsync(userGuid, token, false);
        if (user == null)
            return Error.NotFound("User.NotFound", $"User with ID {userGuid} was not found.");

        var isCorrectPassword = passwordHash.VerifyPassword(changePassword.OldPassword, user.Password);

        if (!isCorrectPassword)
            return Error.Validation(
                "Password.InvalidCurrentPassword",
                "Current password is incorrect");

        if (changePassword.NewPassword != changePassword.ConfirmPassword)
            return Error.Validation(
                "Password.ConfirmMismatch",
                "Confirm password does not match");

        user.Password = passwordHash.HashPassword(changePassword.NewPassword);

        userRepository.Update(user);
        await uow.SaveChangesAsync(token);

        return Result.Updated;
    }

    public async Task<ErrorOr<ResponseProfileDto>> GetProfileAsync(Guid userGuid, CancellationToken token)
    {
        var profile = await userRepository.GetUserByPublicIdAsync(userGuid, token, false);

        if (profile == null)
            return Error.NotFound("User.NotFound", $"User with ID {userGuid} was not found.");

        return new ResponseProfileDto
        {
            UserGuid = userGuid,
            UserName = profile.UserName,
            Email = profile.Email,
            DateOfBirth = profile.DateOfBirth,
            AvatarUrl = profile.AvatarUrl ?? string.Empty
        };
    }

    public async Task<ErrorOr<Updated>> UpdateProfileAsync(Guid userGuid, UpdateProfileDto update,
        CancellationToken token)
    {
        var profile = await userRepository.GetUserByPublicIdAsync(userGuid, token);
        if (profile == null)
            return Error.NotFound("User.NotFound", $"User with ID {userGuid} was not found.");

        profile.UserName = update.UserName ?? profile.UserName;
        profile.DateOfBirth = update.DateOfBirth ?? profile.DateOfBirth;
        profile.AvatarUrl = update.AvatarUrl ?? profile.AvatarUrl;
        profile.UpdatedAt = DateTime.UtcNow;

        userRepository.Update(profile);
        await uow.SaveChangesAsync(token);
        return Result.Updated;
    }
}