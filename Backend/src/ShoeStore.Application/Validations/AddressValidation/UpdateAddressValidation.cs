using FluentValidation;
using ShoeStore.Application.DTOs.AddressDTOs;

namespace ShoeStore.Application.Validations.AddressValidations;

public class UpdateAddressDtoValidation : AbstractValidator<UpdateAddressDto>
{
    public UpdateAddressDtoValidation()
    {
        RuleFor(x => x.ProvinceId)
            .GreaterThan(0).WithMessage("Province is required.");

        RuleFor(x => x.WardId)
            .GreaterThan(0).WithMessage("Ward is required.");

        RuleFor(x => x.DetailAddress)
            .NotEmpty().WithMessage("Detail address is required.")
            .MaximumLength(255).WithMessage("Detail address must not exceed 255 characters.");
    }
}
