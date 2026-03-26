using FluentValidation;
using ShoeStore.Application.DTOs.AuthDTOs;

namespace ShoeStore.Application.Validations.AuthValidation;

public class FacebookAuthDtoValidation : AbstractValidator<FacebookAuthDto>
{
    public FacebookAuthDtoValidation()
    {
        RuleFor(x => x.AccessToken).NotEmpty().WithMessage("Access token is required.");
    }
}