using FluentValidation;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.CartItemDTOs;

namespace ShoeStore.Application.Validations.CartItemDtoValidation;

public class UpdateCartItemValidation : AbstractValidator<UpdateCartItemDto>
{
    public UpdateCartItemValidation()
    {
        RuleFor(x => x.CartItemId)
            .NotEmpty()
            .WithMessage("CartItemId is required.");

        RuleFor(x => x.Quantity)
            .GreaterThanOrEqualTo(0)
            .WithMessage("Quantity must be greater than or equal to 0.");
    }
}