using FluentValidation;
using ShoeStore.Application.DTOs.VoucherDTOs;

namespace ShoeStore.Application.Validations.VoucherValidation;

public class CreateVoucherDtoValidation : AbstractValidator<CreateVoucherDto>
{
    public CreateVoucherDtoValidation()
    {
        RuleFor(x => x.VoucherName)
            .NotEmpty().WithMessage("Voucher name is required")
            .MaximumLength(100).WithMessage("Voucher name must not exceed 100 characters");

        RuleFor(x => x.Discount)
            .NotNull().WithMessage("Discount is required")
            .GreaterThanOrEqualTo(0).WithMessage("Discount must be greater than or equal to 0");

        RuleFor(x => x.TotalQuantity)
            .GreaterThan(0).WithMessage("Total quantity must be greater than 0");

        RuleFor(x => x.ValidFrom)
            .LessThanOrEqualTo(x => x.ValidTo)
            .When(x => x.ValidFrom.HasValue && x.ValidTo.HasValue)
            .WithMessage("Valid from date must be less than or equal to valid to date");
    }
}