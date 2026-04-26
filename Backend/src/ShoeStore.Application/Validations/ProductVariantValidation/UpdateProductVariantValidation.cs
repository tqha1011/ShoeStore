using FluentValidation;
using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.Validations.ProductVariantValidation;

public class UpdateProductVariantValidation : AbstractValidator<UpdateProductVariantDto>
{
    public UpdateProductVariantValidation()
    {
        // SizeId: Must be a valid positive ID
        RuleFor(x => x.SizeId)
            .GreaterThan(0).WithMessage("SizeId must be a positive integer.");

        // ColorIds: Assuming 0 is not a valid ID
        RuleForEach(x => x.ColorIds)
            .GreaterThan(0).WithMessage("ColorId is required and must be greater than 0.");


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