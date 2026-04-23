using FluentValidation;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Domain.Enum;
namespace ShoeStore.Application.Validations.InvoiceValidation
{
    public class UpdateStateRequestDtoValidation : AbstractValidator<UpdateStateRequestDto>
    {
        public UpdateStateRequestDtoValidation()
        {
            RuleFor(x => x.Status)
                .IsInEnum()
                .WithMessage("Invalid status value");

            // Client Only Cancel
            RuleFor(x => x.Status)
                .Must(status => status == InvoiceStatus.Cancelled)
                .WithMessage("You can only cancel the order");
        }
    }
}
