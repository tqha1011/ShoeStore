using FluentValidation;
using ShoeStore.Application.DTOs.AuthDTOs;

namespace ShoeStore.Application.Validations.AuthValidation;

/// <summary>
/// This validator will validate LoginDto
/// If the information provided by the user is not valid, it will return an error message to the user
/// </summary>
public class LoginDtoValidator : AbstractValidator<LoginDto>
{
    public LoginDtoValidator()
    {
        RuleFor(x => x.Password).NotEmpty().WithMessage("Password is required");
        RuleFor(x => x.Email).NotEmpty().WithMessage("Email is required.")
            .EmailAddress().WithMessage("Please enter a valid email address.");
    }
}