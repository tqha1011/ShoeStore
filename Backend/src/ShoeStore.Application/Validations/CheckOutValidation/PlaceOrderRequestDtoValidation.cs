using FluentValidation;
using ShoeStore.Application.DTOs.CheckOutDTOs;

namespace ShoeStore.Application.Validations.CheckOutValidation;

public class PlaceOrderRequestDtoValidation : AbstractValidator<PlaceOrderRequestDto>
{
    public PlaceOrderRequestDtoValidation()
    {
        RuleFor(x => x.Items)
            .NotNull().WithMessage("Items are required.")
            .NotEmpty().WithMessage("At least one item is required.");

        RuleForEach(x => x.Items)
            .SetValidator(new CheckOutDtoValidation());

        RuleFor(x => x.VoucherIds)
            .Must(voucherIds => voucherIds == null || voucherIds.Count == voucherIds.Distinct().Count())
            .WithMessage("VoucherIds must not contain duplicates.");

        RuleFor(x => x.VoucherIds)
            .Must(voucherIds => voucherIds == null || voucherIds.All(voucherId => voucherId > 0))
            .WithMessage("Each voucherId must be greater than 0.");

        RuleFor(x => x.FullName)
            .NotEmpty().WithMessage("FullName is required.")
            .MaximumLength(100).WithMessage("FullName cannot exceed 100 characters.");

        RuleFor(x => x.Address)
            .NotEmpty().WithMessage("Address is required.")
            .MaximumLength(255).WithMessage("Address cannot exceed 255 characters.");

        RuleFor(x => x.PaymentId)
            .GreaterThan(0)
            .WithMessage("PaymentId must be greater than 0.");

        RuleFor(x => x.PhoneNumber)
            .NotEmpty().WithMessage("PhoneNumber is required.")
            .Matches(@"^\+?\d{9,15}$")
            .WithMessage("PhoneNumber must be a valid phone number format.");
    }
}