namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record SearchResultDto(
    string Status,
    string Message,
    List<ProductSummaryForLlm> Products);

public sealed record ProductSummaryForLlm(
    Guid PublicId,
    string ProductName,
    string ProductBrand,
    string? CategoryName
);