using FluentValidation;
using ShoeStore.Application.DTOs;

namespace ShoeStore.Application.Validations.CheckOutValidation;

public class CheckOutDtoValidation : AbstractValidator<CheckOutDto>
{
    public CheckOutDtoValidation()
    {
        RuleFor(x => x.VariantId).NotEmpty().WithMessage("Product is required.");

        RuleFor(x => x.Quantity).NotEmpty().WithMessage("Quantity is required.")
            .GreaterThan(0).WithMessage("Quantity must be greater than 0.");
    }
}