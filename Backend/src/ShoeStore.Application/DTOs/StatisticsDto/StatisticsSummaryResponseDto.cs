namespace ShoeStore.Application.DTOs.StatisticsDto;

public sealed record StatisticsSummaryResponseDto(
    decimal TotalRevenue,
    int TotalOrders,
    decimal AverageRevenue,
    decimal GrowthInvoicePercent,
    decimal GrowthAverageRevenuePercent,
    decimal GrowthTotalRevenuePercent);