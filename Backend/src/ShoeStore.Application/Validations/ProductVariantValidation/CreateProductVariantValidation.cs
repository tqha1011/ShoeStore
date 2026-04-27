using FluentValidation;
using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.Validations.ProductVariantValidation;

public class CreateProductVariantValidation : AbstractValidator<CreateProductVariantDto>
{
    public CreateProductVariantValidation()
    {
        RuleFor(x => x.SizeId)
            .NotEmpty().WithMessage("Size ID is required.")
            .GreaterThan(0).WithMessage("Size ID must be a positive integer.");

        RuleFor(x => x.ColorId)
            .NotEmpty().WithMessage("Color ID is required.")
            .GreaterThan(0).WithMessage("Color ID must be a positive integer.");

        RuleFor(x => x.Stock)
            .NotEmpty().WithMessage("Stock quantity is required.")
            .GreaterThanOrEqualTo(0).WithMessage("Stock quantity cannot be negative.");

        RuleFor(x => x.Price)
            .NotEmpty().WithMessage("Price is required.")
            .GreaterThan(0).WithMessage("Price must be greater than 0.")
            .LessThanOrEqualTo(999999.99m).WithMessage("Price cannot exceed 999,999.99.")
            .Must(x => x.ToString().Split('.').Length == 1 || x.ToString().Split('.')[1].Length <= 2)
            .WithMessage("Price cannot have more than 2 decimal places.");

        RuleFor(x => x.ImageUrl)
            .Must(uri => string.IsNullOrEmpty(uri) || Uri.TryCreate(uri, UriKind.Absolute, out _))
            .WithMessage("Invalid Image URL format.")
            .MaximumLength(500).WithMessage("Image URL is too long (max 500 characters).")
            .When(x => !string.IsNullOrEmpty(x.ImageUrl));

        RuleFor(x => x.IsSelling)
            .NotNull().WithMessage("IsSelling status is required.");
    }
}