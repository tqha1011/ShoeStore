using FluentValidation;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Validations.ProductValidation;

public class ProductBaseValidation<T> : AbstractValidator<T>where T : ProductBaseDto
{
    public ProductBaseValidation()
    {
        RuleFor(x => x.ProductName)
            .NotEmpty().WithMessage("Product name is required.")
            .MinimumLength(3).WithMessage("Product name must be at least 3 characters long.")
            .MaximumLength(100).WithMessage("Product name cannot exceed 100 characters.");

        RuleFor(x => x.Brand)
            .MaximumLength(50).WithMessage("Brand name cannot exceed 50 characters.")
            .When(x => !string.IsNullOrEmpty(x.Brand));
    }
}