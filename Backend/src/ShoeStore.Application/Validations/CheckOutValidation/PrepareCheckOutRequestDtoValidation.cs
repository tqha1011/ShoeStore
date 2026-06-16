using FluentValidation;
using ShoeStore.Application.DTOs.CheckOutDTOs;

namespace ShoeStore.Application.Validations.CheckOutValidation;

public class PrepareCheckOutRequestDtoValidation : AbstractValidator<PrepareCheckOutRequestDto>
{
    public PrepareCheckOutRequestDtoValidation()
    {
        RuleFor(x => x.VoucherIds)
            .NotNull().WithMessage("VoucherIds must not be null")
            .Must(voucherIds => voucherIds.Count == voucherIds.Distinct().Count())
            .WithMessage("VoucherIds must not contain duplicates.")
            .Must(voucherIds => voucherIds.All(voucherId => voucherId > 0))
            .WithMessage("Each voucherId must be greater than 0.");
    }
}