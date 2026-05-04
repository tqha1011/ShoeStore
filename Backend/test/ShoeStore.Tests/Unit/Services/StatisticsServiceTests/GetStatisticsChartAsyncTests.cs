using Microsoft.Extensions.Caching.Hybrid;
using Microsoft.Extensions.DependencyInjection;
using Moq;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Services;

namespace ShoeStore.Tests.Unit.Services.StatisticsServiceTests;

public class GetStatisticsChartAsyncTests
{
    private readonly Mock<IInvoiceRepository> _invoiceRepository = new();
    private readonly StatisticsService _statisticsService;

    public GetStatisticsChartAsyncTests()
    {
        var services = new ServiceCollection();
        services.AddHybridCache();
        var serviceProvider = services.BuildServiceProvider();
        var cache = serviceProvider.GetRequiredService<HybridCache>();

        _statisticsService = new StatisticsService(_invoiceRepository.Object, cache);
    }

    [Fact]
    public async Task GetStatisticsChartAsync_WhenTypeWeek_ReturnsSevenDaysAndZeroForMissingDates()
    {
        var endDate = DateTime.UtcNow.ToVnTime();
        var startDate = endDate.AddDays(-6).Date;

        var rawData = new List<(DateTime Date, decimal Revenue)>
        {
            (startDate, 100m),
            (startDate.AddDays(2), 250m)
        };

        _invoiceRepository
            .Setup(repo => repo.GetChartDataAsync(
                It.IsAny<DateTime>(), It.IsAny<DateTime>(), It.IsAny<string>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(rawData);

        var result = await _statisticsService.GetStatisticsChartAsync("week", CancellationToken.None);

        Assert.False(result.IsError);
        Assert.Equal(7, result.Value.ChartData.Count);

        var missingLabel = startDate.AddDays(1).ToString("dd/MM");
        var missingEntry = result.Value.ChartData.First(item => item.DateLabel == missingLabel);
        Assert.Equal(0m, missingEntry.Revenue);

        var providedLabel = startDate.ToString("dd/MM");
        var providedEntry = result.Value.ChartData.First(item => item.DateLabel == providedLabel);
        Assert.Equal(100m, providedEntry.Revenue);
    }

    [Fact]
    public async Task GetStatisticsChartAsync_WhenType30Days_ReturnsAllDaysAndZeroForMissingDates()
    {
        var endDate = DateTime.UtcNow.ToVnTime();
        var startDate = endDate.AddMonths(-1).Date;
        var expectedCount = (endDate.Date - startDate.Date).Days + 1;

        var rawData = new List<(DateTime Date, decimal Revenue)>
        {
            (startDate, 100m),
            (startDate.AddDays(10), 300m)
        };

        _invoiceRepository
            .Setup(repo => repo.GetChartDataAsync(
                It.IsAny<DateTime>(), It.IsAny<DateTime>(), It.IsAny<string>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(rawData);

        var result = await _statisticsService.GetStatisticsChartAsync("30days", CancellationToken.None);

        Assert.False(result.IsError);
        Assert.Equal(expectedCount, result.Value.ChartData.Count);

        var missingLabel = startDate.AddDays(1).ToString("dd/MM");
        var missingEntry = result.Value.ChartData.First(item => item.DateLabel == missingLabel);
        Assert.Equal(0m, missingEntry.Revenue);
    }

    [Fact]
    public async Task GetStatisticsChartAsync_WhenType12Months_ReturnsAllMonthsAndZeroForMissingMonths()
    {
        var endDate = DateTime.UtcNow.ToVnTime();
        var startDate = endDate.AddYears(-1).Date;
        var startMonth = new DateTime(startDate.Year, startDate.Month, 1);
        var endMonth = new DateTime(endDate.Year, endDate.Month, 1);
        var expectedCount = (endMonth.Year - startMonth.Year) * 12 + (endMonth.Month - startMonth.Month) + 1;

        var rawData = new List<(DateTime Date, decimal Revenue)>
        {
            (startMonth, 500m)
        };

        _invoiceRepository
            .Setup(repo => repo.GetChartDataAsync(
                It.IsAny<DateTime>(), It.IsAny<DateTime>(), It.IsAny<string>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(rawData);

        var result = await _statisticsService.GetStatisticsChartAsync("12months", CancellationToken.None);

        Assert.False(result.IsError);
        Assert.Equal(expectedCount, result.Value.ChartData.Count);

        var missingLabel = startMonth.AddMonths(1).ToString("MM/yy");
        var missingEntry = result.Value.ChartData.First(item => item.DateLabel == missingLabel);
        Assert.Equal(0m, missingEntry.Revenue);

        var providedLabel = startMonth.ToString("MM/yy");
        var providedEntry = result.Value.ChartData.First(item => item.DateLabel == providedLabel);
        Assert.Equal(500m, providedEntry.Revenue);
    }
}

