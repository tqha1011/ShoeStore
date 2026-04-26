using FluentValidation;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Validations.ProductValidation;

public class CreateProductValidation : AbstractValidator<CreateProductDto>
{
    public CreateProductValidation()
    {
        RuleFor(x => x.ProductName)
            .NotEmpty().WithMessage("Product name is required.")
            .MinimumLength(3).WithMessage("Product name must be at least 3 characters long.")
            .MaximumLength(100).WithMessage("Product name cannot exceed 100 characters.");

        RuleFor(x => x.CategoryId)
            .GreaterThan(0).WithMessage("Valid Category ID is required.");
    }
}