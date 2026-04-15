namespace ShoeStore.Application.DTOs.StatisticsDto;

public sealed record ProductHighestStatisticsDto(
    Guid ProductPublicId,
    int VariantId,
    string ProductName,
    string? ImageUrl,
    int TotalInvoices,
    decimal TotalRevenue);