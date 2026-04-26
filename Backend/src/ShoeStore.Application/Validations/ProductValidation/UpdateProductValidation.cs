using FluentValidation;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Validations.ProductValidation;

public class UpdateProductValidation : ProductBaseValidation<UpdateProductDto>
{
    public UpdateProductValidation()
    {
        RuleFor(x => x.ProductVariants)
            .NotNull().WithMessage("Product variants must not be null")
            .NotEmpty().WithMessage("At least one product variant is required");
    }
}