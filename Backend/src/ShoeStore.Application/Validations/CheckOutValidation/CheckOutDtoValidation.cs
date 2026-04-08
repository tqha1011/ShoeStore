using FluentValidation;
using ShoeStore.Application.DTOs.CheckOutDTOs;

namespace ShoeStore.Application.Validations.CheckOutValidation;

public class CheckOutDtoValidation : AbstractValidator<CheckOutRequestDto>
{
    public CheckOutDtoValidation()
    {
        RuleFor(x => x.VariantId)
            .NotEmpty()
            .WithMessage("Product Variant is required.");

        RuleFor(x => x.Quantity)
            .NotEmpty().WithMessage("Quantity is required.")
            .GreaterThan(0).WithMessage("Quantity must be greater than 0.");
    }
}