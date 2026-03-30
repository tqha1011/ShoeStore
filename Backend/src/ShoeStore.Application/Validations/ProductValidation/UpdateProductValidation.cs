using FluentValidation;
using ShoeStore.Application.DTOs.ProductDTOs;
namespace ShoeStore.Application.Validations.ProductValidation
{
    public class UpdateProductValidation : ProductBaseValidation<UpdateProductDto>
    {
        public UpdateProductValidation()
        {
            RuleFor(x => x.Variants)
                        .NotNull().WithMessage("Variants must not be null")
                        .NotEmpty().WithMessage("At least one variant is required");
        }
    }
}
