using FluentValidation;
using ShoeStore.Application.DTOs.InvoiceDTOs;

namespace ShoeStore.Application.Validations.InvoiceValidation;

public class UpdateStateRequestDtoValidation : AbstractValidator<UpdateStateRequestDto>
{
    public UpdateStateRequestDtoValidation()
    {
        RuleFor(x => x.Status)
            .IsInEnum()
            .WithMessage("Invalid status value");
    }
}