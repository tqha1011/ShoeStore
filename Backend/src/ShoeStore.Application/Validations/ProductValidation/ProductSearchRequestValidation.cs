using FluentValidation;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Validations.ProductValidation;

public class ProductSearchRequestValidation : AbstractValidator<ProductSearchRequest>
{
    public ProductSearchRequestValidation()
    {
        // 1. Keyword & Brand: Just limit the length to prevent SQL injection or DOS
        RuleFor(x => x.Keyword)
            .MaximumLength(100).WithMessage("Keyword is too long.");

        RuleFor(x => x.Brand)
            .MaximumLength(50).WithMessage("Brand name is too long.");

        // 2. IDs: If provided, they must be valid positive numbers
        RuleFor(x => x.ProductId)
            .GreaterThan(0).WithMessage("ProductId must be greater than 0.")
            .When(x => x.ProductId.HasValue);

        // 3. Collections: Ensure IDs inside the lists are valid
        RuleForEach(x => x.ListColorId)
            .GreaterThan(0).WithMessage("ColorId must be a positive integer.")
            .When(x => x.ListColorId != null);

        RuleForEach(x => x.ListSizeId)
            .GreaterThan(0).WithMessage("SizeId must be a positive integer.")
            .When(x => x.ListSizeId != null);

        // 4. Price Logic: Min must be >= 0, Max must be >= Min
        RuleFor(x => x.MinPrice)
            .GreaterThanOrEqualTo(0).WithMessage("Min Price cannot be negative.")
            .When(x => x.MinPrice.HasValue);

        RuleFor(x => x.MaxPrice)
            .GreaterThan(0).WithMessage("Max Price must be greater than 0.")
            .GreaterThanOrEqualTo(x => x.MinPrice ?? 0)
            .WithMessage("Max Price must be greater than or equal to Min Price.")
            .When(x => x.MaxPrice.HasValue);

        // 5. Sorting: Restrict to allowed values to prevent errors in Query logic
        var allowedSorts = new[] { "default", "price_asc", "price_desc", "newest" };
        RuleFor(x => x.Sort)
            .Must(x => allowedSorts.Contains(x?.ToLower()))
            .WithMessage($"Sort must be one of the following: {string.Join(", ", allowedSorts)}");

        // 6. Pagination: (Optional) Even though you have logic in the DTO, 
        // you can still add rules here for strict API contracts.
        RuleFor(x => x.PageSize)
            .InclusiveBetween(1, 50).WithMessage("PageSize must be between 1 and 50.");
    }
}