using FluentValidation;
using ShoeStore.Application.DTOs.AddressDTOs;

namespace ShoeStore.Application.Validations.AddressValidations;

public class CreateAddressDtoValidation : AbstractValidator<CreateAddressDto>
{
    public CreateAddressDtoValidation()
    {
        RuleFor(x => x.Province)
            .NotEmpty()
            .WithMessage("Province is required.")
            .MaximumLength(100)
            .WithMessage("Province must not exceed 100 characters.");

        RuleFor(x => x.District)
            .NotEmpty()
            .WithMessage("District is required.")
            .MaximumLength(100)
            .WithMessage("District must not exceed 100 characters.");

        RuleFor(x => x.DetailAddress)
            .NotEmpty()
            .WithMessage("Detail address is required.")
            .MaximumLength(255)
            .WithMessage("Detail address must not exceed 255 characters.");
    }
}