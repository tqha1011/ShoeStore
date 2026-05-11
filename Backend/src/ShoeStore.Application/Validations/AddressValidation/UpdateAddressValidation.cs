using FluentValidation;
using ShoeStore.Application.DTOs.AddressDTOs;

namespace ShoeStore.Application.Validations.AddressValidations;

public class UpdateAddressDtoValidation : AbstractValidator<UpdateAddressDto>
{
    public UpdateAddressDtoValidation()
    {
        RuleFor(x => x.Province)
            .MaximumLength(100)
            .WithMessage("Province must not exceed 100 characters.");

        RuleFor(x => x.District)
            .MaximumLength(100)
            .WithMessage("District must not exceed 100 characters.");

        RuleFor(x => x.DetailAddress)
            .MaximumLength(255)
            .WithMessage("Detail address must not exceed 255 characters.");
    }
}