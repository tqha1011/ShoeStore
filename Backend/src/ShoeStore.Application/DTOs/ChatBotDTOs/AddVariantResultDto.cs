namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record AddVariantResultDto(
    string Status,
    string Message,
    VariantResultDto? Variant);

public sealed record VariantResultDto(
    int SizeId,
    decimal Size,
    int ColorId,
    string ColorName,
    int Stock,
    decimal Price,
    string? ImageUrl);