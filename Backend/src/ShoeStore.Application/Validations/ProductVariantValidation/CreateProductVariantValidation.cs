using FluentValidation;
using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.Validations.ProductVariantValidation;

public class CreateProductVariantValidator : AbstractValidator<CreateProductVariantDto>
{
    public CreateProductVariantValidator()
    {
        // SizeId: Must be a valid positive ID
        RuleFor(x => x.SizeId)
            .GreaterThan(0).WithMessage("SizeId must be a positive integer.");

        // Size: Optional, but if provided, must be within a reasonable range
        RuleFor(x => x.Size)
            .InclusiveBetween(1, 100).WithMessage("Size must be between 1 and 100.")
            .When(x => x.Size.HasValue);

        // ColorId: Assuming 0 is not a valid ID
        RuleFor(x => x.ColorId)
            .GreaterThan(0).WithMessage("ColorId is required and must be greater than 0.");

        // ColorName: Optional, but cannot be empty strings if provided
        RuleFor(x => x.ColorName)
            .MaximumLength(50).WithMessage("Color name cannot exceed 50 characters.")
            .Must(name => string.IsNullOrWhiteSpace(name) || name.Trim().Length > 0)
            .WithMessage("Color name cannot be empty or whitespace.");

        // Stock: Cannot be negative
        RuleFor(x => x.Stock)
            .GreaterThanOrEqualTo(0).WithMessage("Stock quantity cannot be less than zero.");

        // Price: Must be a positive decimal
        RuleFor(x => x.Price)
            .GreaterThan(0).WithMessage("Price must be greater than 0.")
            .PrecisionScale(18, 2, false).WithMessage("Price cannot have more than 2 decimal places.");

        // ImageUrl: Basic URL validation if it's not null
        RuleFor(x => x.ImageUrl)
            .Must(uri => string.IsNullOrEmpty(uri) || Uri.TryCreate(uri, UriKind.Absolute, out _))
            .WithMessage("Invalid Image URL format.")
            .MaximumLength(255).WithMessage("URL is too long.");
    }
}