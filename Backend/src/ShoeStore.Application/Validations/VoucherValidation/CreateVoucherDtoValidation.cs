using FluentValidation;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Domain.Enum;

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
            .GreaterThan(0).WithMessage("Discount must be greater than 0")
            .When(x => x.DiscountType == DiscountType.FixedAmount);
        
        RuleFor(x => x.Discount)
            .NotNull().WithMessage("Discount is required")
            .GreaterThan(0).WithMessage("Discount must be greater than 0")
            .LessThanOrEqualTo(100).WithMessage("Discount must be less than or equal to 100%")
            .When(x => x.DiscountType == DiscountType.Percentage);

        RuleFor(x => x.TotalQuantity)
            .GreaterThan(0).WithMessage("Total quantity must be greater than 0");

        RuleFor(x => x.ValidFrom)
            .LessThanOrEqualTo(x => x.ValidTo)
            .WithMessage("Valid from date must be less than or equal to valid to date")
            .When(x => x.ValidFrom.HasValue && x.ValidTo.HasValue)
            .WithMessage("Valid from date must be less than or equal to valid to date");

        RuleFor(x => x.ValidTo)
            .GreaterThanOrEqualTo(DateTime.Now).WithMessage("Valid to date")
            .When(x => x.ValidTo.HasValue);
    }
}