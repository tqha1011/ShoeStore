using FluentValidation;
using ShoeStore.Application.DTOs.AuthDTOs;

namespace ShoeStore.Application.Validations.AuthValidation;

public class GoogleLoginDtoValidation : AbstractValidator<GoogleLoginDto>
{
    public  GoogleLoginDtoValidation()
    {
        RuleFor(x => x.IdToken)
            .NotEmpty().WithMessage("The Google IdToken is required.");
    }
}