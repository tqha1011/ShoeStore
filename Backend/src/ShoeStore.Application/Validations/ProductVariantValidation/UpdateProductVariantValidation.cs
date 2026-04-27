using FluentValidation;
using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.Validations.ProductVariantValidation;

public class UpdateProductVariantValidation : AbstractValidator<UpdateProductVariantDto>
{
    public UpdateProductVariantValidation()
    {
        RuleFor(x => x.SizeId)
            .GreaterThan(0).WithMessage("Size ID must be a positive integer.")
            .When(x => x.SizeId.HasValue);

        RuleFor(x => x.ColorId)
            .GreaterThan(0).WithMessage("Color ID must be a positive integer.")
            .When(x => x.ColorId.HasValue);

        RuleFor(x => x.Stock)
            .GreaterThanOrEqualTo(0).WithMessage("Stock quantity cannot be negative.")
            .When(x => x.Stock.HasValue);

        RuleFor(x => x.Price)
            .GreaterThan(0).WithMessage("Price must be greater than 0.")
            .LessThanOrEqualTo(999999.99m).WithMessage("Price cannot exceed 999,999.99.")
            .Must(x => x.Value.ToString().Split('.').Length == 1 || x.Value.ToString().Split('.')[1].Length <= 2)
            .WithMessage("Price cannot have more than 2 decimal places.")
            .When(x => x.Price.HasValue);

        RuleFor(x => x.ImageUrl)
            .Must(uri => string.IsNullOrEmpty(uri) || Uri.TryCreate(uri, UriKind.Absolute, out _))
            .WithMessage("Invalid Image URL format.")
            .MaximumLength(500).WithMessage("Image URL is too long (max 500 characters).")
            .When(x => !string.IsNullOrEmpty(x.ImageUrl));

        RuleFor(x => x.IsSelling)
            .NotNull().WithMessage("IsSelling status must be a valid boolean value.")
            .When(x => x.IsSelling.HasValue);
    }
}