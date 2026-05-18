using FluentValidation;
using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Validations.ChatBotDtoValidation;

public class CreateCampaignRequestDtoValidation : AbstractValidator<CreateCampaignRequestDto>
{
    public CreateCampaignRequestDtoValidation()
    {
        RuleFor(x => x.Content)
            .NotEmpty()
            .WithMessage("Content is required")
            .MaximumLength(1000)
            .WithMessage("Content must not exceed 1000 characters");

        RuleFor(x => x.PublicSessionId)
            .NotEmpty()
            .WithMessage("Public session id is required");
    }
}