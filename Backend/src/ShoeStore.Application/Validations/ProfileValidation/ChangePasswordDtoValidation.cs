using FluentValidation;
using ShoeStore.Application.DTOs.ProfileDTOs;

namespace ShoeStore.Application.Validations.ProfileValidation;

public class ChangePasswordDtoValidator : AbstractValidator<ChangePasswordDto>
{
    public ChangePasswordDtoValidator()
    {
        RuleFor(x => x.OldPassword)
            .NotEmpty()
            .WithMessage("Old password is required");

        RuleFor(x => x.NewPassword)
            .NotEmpty()
            .WithMessage("New password is required")
            .MinimumLength(8)
            .WithMessage("New password must be at least 8 characters")
            .Matches("[A-Z]")
            .WithMessage("New password must contain at least one uppercase letter")
            .Matches("[a-z]")
            .WithMessage("New password must contain at least one lowercase letter")
            .Matches("[0-9]")
            .WithMessage("New password must contain at least one number");

        RuleFor(x => x.ConfirmPassword)
            .NotEmpty()
            .WithMessage("Confirm password is required")
            .Equal(x => x.NewPassword)
            .WithMessage("Confirm password does not match");

        RuleFor(x => x)
            .Must(x => x.OldPassword != x.NewPassword)
            .WithMessage("New password must be different from old password");
    }
}