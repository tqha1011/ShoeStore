using Microsoft.Extensions.Caching.Hybrid;
using Microsoft.Extensions.DependencyInjection;
using Moq;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Services;

namespace ShoeStore.Tests.Unit.Services.StatisticsServiceTests;

public class GetProductsHighestStatisticsAsyncTests
{
    private readonly Mock<IInvoiceRepository> _invoiceRepository = new();
    private readonly StatisticsService _statisticsService;

    public GetProductsHighestStatisticsAsyncTests()
    {
        var services = new ServiceCollection();
        services.AddHybridCache();
        var serviceProvider = services.BuildServiceProvider();
        var cache = serviceProvider.GetRequiredService<HybridCache>();

        _statisticsService = new StatisticsService(_invoiceRepository.Object, cache);
    }

    [Fact]
    public async Task GetProductsHighestStatisticsAsync_WhenPreviousMonthMissing_ReturnsHundredGrowth()
    {
        var productId = Guid.NewGuid();
        var current = new List<ProductHighestStatisticsDto>
        {
            new(productId, "Jordan", "image.png", 5, 500m)
        };

        _invoiceRepository
            .SetupSequence(repo => repo.GetTop3ProductsAsync(
                It.IsAny<DateTime>(), It.IsAny<DateTime>(), It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(current)
            .ReturnsAsync([]);

        var result = await _statisticsService.GetProductsHighestStatisticsAsync(CancellationToken.None);

        Assert.False(result.IsError);
        Assert.Single(result.Value);
        Assert.Equal(100m, result.Value[0].GrowthRevenuePercentage);
    }

    [Fact]
    public async Task GetProductsHighestStatisticsAsync_WhenPreviousMonthExists_ReturnsCalculatedGrowth()
    {
        var productId = Guid.NewGuid();
        var current = new List<ProductHighestStatisticsDto>
        {
            new(productId, "Jordan", null, 5, 200m)
        };
        var previous = new List<ProductHighestStatisticsDto>
        {
            new(productId, "Jordan", null, 4, 100m)
        };

        _invoiceRepository
            .SetupSequence(repo => repo.GetTop3ProductsAsync(
                It.IsAny<DateTime>(), It.IsAny<DateTime>(), It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(current)
            .ReturnsAsync(previous);

        var result = await _statisticsService.GetProductsHighestStatisticsAsync(CancellationToken.None);

        Assert.False(result.IsError);
        Assert.Single(result.Value);
        Assert.Equal(100m, result.Value[0].GrowthRevenuePercentage);
        Assert.Equal(string.Empty, result.Value[0].ImageUrl);
    }
}

