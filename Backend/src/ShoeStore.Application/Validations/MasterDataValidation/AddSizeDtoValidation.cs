using FluentValidation;
using ShoeStore.Application.DTOs.MasterDataDTOs;

namespace ShoeStore.Application.Validations.MasterDataValidation;

public class AddSizeDtoValidation : AbstractValidator<AddSizeDto>
{
    public AddSizeDtoValidation()
    {
        RuleFor(x => x.Size)
            .GreaterThan(0).WithMessage("Size must be greater than 0.")
            .LessThanOrEqualTo(100).WithMessage("Size must be less than or equal to 100.")
            .PrecisionScale(5, 2, false).WithMessage("Size can have up to 2 decimal places.");
    }
}