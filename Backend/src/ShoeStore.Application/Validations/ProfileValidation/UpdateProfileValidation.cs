using FluentValidation;
using ShoeStore.Application.DTOs.ProfileDTOs;

namespace ShoeStore.Application.Validations.ProfileValidation;

/// <summary>
/// This validator will validate UpdateProfileDto
/// </summary>
public class UpdateProfileDtoValidator : AbstractValidator<UpdateProfileDto>
{
    public UpdateProfileDtoValidator()
    {
        RuleFor(x => x.UserName)
            .NotEmpty().WithMessage("Username is required")
            .MaximumLength(50).WithMessage("Username must not exceed 50 characters");

        RuleFor(x => x.DateOfBirth)
            .LessThan(DateTime.UtcNow).WithMessage("Date of birth must be in the past")
            .When(x => x.DateOfBirth.HasValue);

        RuleFor(x => x.AvatarUrl)
            .MaximumLength(500).WithMessage("Avatar URL must not exceed 500 characters")
            .When(x => !string.IsNullOrEmpty(x.AvatarUrl));
    }
}
