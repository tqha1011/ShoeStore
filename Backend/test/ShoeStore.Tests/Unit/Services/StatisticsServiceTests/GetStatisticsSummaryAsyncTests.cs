using Microsoft.Extensions.Caching.Hybrid;
using Microsoft.Extensions.DependencyInjection;
using Moq;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Services;

namespace ShoeStore.Tests.Unit.Services.StatisticsServiceTests;

public class GetStatisticsSummaryAsyncTests
{
    private readonly Mock<IInvoiceRepository> _invoiceRepository = new();
    private readonly StatisticsService _statisticsService;

    public GetStatisticsSummaryAsyncTests()
    {
        var services = new ServiceCollection();
        services.AddHybridCache();
        var serviceProvider = services.BuildServiceProvider();
        var cache = serviceProvider.GetRequiredService<HybridCache>();

        _statisticsService = new StatisticsService(_invoiceRepository.Object, cache);
    }

    [Fact]
    public async Task GetStatisticsSummaryAsync_WhenMetricsProvided_ReturnsCalculatedGrowth()
    {
        var current = (TotalInvoices: 15, TotalRevenue: 1500m);
        var previous = (TotalInvoices: 10, TotalRevenue: 1000m);

        _invoiceRepository
            .SetupSequence(repo => repo.GetSummaryMetricsAsync(
                It.IsAny<DateTime>(), It.IsAny<DateTime>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(current)
            .ReturnsAsync(previous);

        var result = await _statisticsService.GetStatisticsSummaryAsync(CancellationToken.None);

        Assert.False(result.IsError);
        Assert.Equal(1500m, result.Value.TotalRevenue);
        Assert.Equal(15, result.Value.TotalOrders);
        Assert.Equal(100m, result.Value.AverageRevenue);
        Assert.Equal(50m, result.Value.GrowthAverageRevenuePercent);
        Assert.Equal(50m, result.Value.GrowthInvoicePercent);
        Assert.Equal(0m, result.Value.GrowthTotalRevenuePercent);
    }

    [Fact]
    public async Task GetStatisticsSummaryAsync_WhenPreviousMetricsZero_ReturnsHundredPercentGrowth()
    {
        var current = (TotalInvoices: 5, TotalRevenue: 200m);
        var previous = (TotalInvoices: 0, TotalRevenue: 0m);

        _invoiceRepository
            .SetupSequence(repo => repo.GetSummaryMetricsAsync(
                It.IsAny<DateTime>(), It.IsAny<DateTime>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(current)
            .ReturnsAsync(previous);

        var result = await _statisticsService.GetStatisticsSummaryAsync(CancellationToken.None);

        Assert.False(result.IsError);
        Assert.Equal(200m, result.Value.TotalRevenue);
        Assert.Equal(5, result.Value.TotalOrders);
        Assert.Equal(40m, result.Value.AverageRevenue);
        Assert.Equal(100m, result.Value.GrowthAverageRevenuePercent);
        Assert.Equal(100m, result.Value.GrowthInvoicePercent);
        Assert.Equal(100m, result.Value.GrowthTotalRevenuePercent);
    }
}
