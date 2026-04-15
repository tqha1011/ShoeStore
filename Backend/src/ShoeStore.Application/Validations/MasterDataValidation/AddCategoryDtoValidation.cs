using FluentValidation;
using ShoeStore.Application.DTOs.MasterDataDTOs;

namespace ShoeStore.Application.Validations.MasterDataValidation;

public class AddCategoryDtoValidation : AbstractValidator<AddCategoryDto>
{
    public AddCategoryDtoValidation()
    {
        RuleFor(x => x.CategoryName)
            .NotEmpty().WithMessage("Category name is required.")
            .Must(name => !string.IsNullOrWhiteSpace(name)).WithMessage("Category name cannot be whitespace.")
            .MaximumLength(100).WithMessage("Category name cannot exceed 100 characters.");
    }
}

