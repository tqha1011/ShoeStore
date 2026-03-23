using FluentValidation;
using ShoeStore.Application.DTOs.RestorePasswordDto;

namespace ShoeStore.Application.Validations.RestorePassValidation;

public class OtpVerifyDtoValidtion : AbstractValidator<OtpVerifyDto>
{
    public OtpVerifyDtoValidtion()
    {
        RuleFor(o => o.Otp).NotEmpty().WithMessage("Otp is required");
        RuleFor(o => o.Email).NotEmpty().WithMessage("Email is required");
    }
}