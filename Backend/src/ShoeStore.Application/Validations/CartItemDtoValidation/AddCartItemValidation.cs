using FluentValidation;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.CartItemDTOs;

namespace ShoeStore.Application.Validations.CartItemDtoValidation;

public class AddCartItemValidation : AbstractValidator<AddCartItemDto>
{
    public AddCartItemValidation()
    {
        RuleFor(x => x.VariantPublicId).NotEmpty().WithMessage("VariantId is required.");

        RuleFor(x => x.Quantity)
            .GreaterThan(0)
            .WithMessage("Quantity must be greater than 0.");
    }
}