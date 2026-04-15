using FluentValidation;
using ShoeStore.Application.DTOs.MasterDataDTOs;

namespace ShoeStore.Application.Validations.MasterDataValidation;

public class AddColorDtoValidation : AbstractValidator<AddColorDto>
{
    public AddColorDtoValidation()
    {
        RuleFor(x => x.ColorName)
            .NotEmpty().WithMessage("Color name is required.")
            .Must(name => !string.IsNullOrWhiteSpace(name)).WithMessage("Color name cannot be whitespace.")
            .MaximumLength(50).WithMessage("Color name cannot exceed 50 characters.");
    }
}