namespace ShoeStore.Application.DTOs.StatisticsDto;

public sealed record ProductHighestStatisticsResponseDto(
    Guid ProductPublicId,
    string ProductName,
    string? ImageUrl,
    int TotalInvoices,
    decimal TotalRevenue,
    decimal GrowthRevenuePercentage);