using FluentValidation;
using ShoeStore.Application.DTOs.RestorePasswordDto;

namespace ShoeStore.Application.Validations.RestorePassValidation;

public class UpdatePasswordDtoValidation : AbstractValidator<UpdatePasswordDto>
{
    public UpdatePasswordDtoValidation()
    {
        RuleFor(x => x.Email).NotEmpty().WithMessage("Email is required")
            .EmailAddress().WithMessage("Please enter a valid email address.");

        RuleFor(x => x.Otp).NotEmpty().WithMessage("Otp is required");

        RuleFor(x => x.NewPassword).NotEmpty().WithMessage("Password is required.")
            .MinimumLength(8).WithMessage("Password must be at least 8 characters long.")
            .Matches(@"^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$")
            .WithMessage(
                "Password must contain at least one uppercase letter, one lowercase letter, one number, and one special character.");
    }
}