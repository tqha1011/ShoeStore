using FluentValidation;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Validations.ProductValidation;

public class ProductBaseValidation<T> : AbstractValidator<T> where T : ProductBaseDto
{
    public ProductBaseValidation()
    {
        RuleFor(x => x.ProductName)
            .NotEmpty().WithMessage("Product name is required.")
            .MinimumLength(3).WithMessage("Product name must be at least 3 characters long.")
            .MaximumLength(100).WithMessage("Product name cannot exceed 100 characters.")
            .Matches(@"^[a-zA-Z0-9\s\-()&.,/']+$").WithMessage("Product name contains invalid characters.");

        RuleFor(x => x.Brand)
            .NotEmpty().WithMessage("Brand is required.")
            .MinimumLength(2).WithMessage("Brand must be at least 2 characters long.")
            .MaximumLength(50).WithMessage("Brand name cannot exceed 50 characters.")
            .Matches(@"^[a-zA-Z0-9\s\-&.()]+$").WithMessage("Brand contains invalid characters.")
            .When(x => !string.IsNullOrEmpty(x.Brand));

        RuleFor(x => x.CategoryId)
            .NotEmpty().WithMessage("Category ID is required.")
            .GreaterThan(0).WithMessage("Category ID must be a positive integer.")
            .When(x => x.CategoryId.HasValue);
    }
}