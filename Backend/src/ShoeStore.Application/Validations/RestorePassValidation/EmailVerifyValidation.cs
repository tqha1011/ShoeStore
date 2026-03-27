using FluentValidation;
using ShoeStore.Application.DTOs.RestorePasswordDto;

namespace ShoeStore.Application.Validations.RestorePassValidation;

public class EmailVerifyValidation : AbstractValidator<EmailVerifyDto>
{
    public EmailVerifyValidation()
    {
        RuleFor(x => x.Email).EmailAddress().WithMessage("Invalid Email")
            .NotEmpty().WithMessage("Email is required");
    }
}