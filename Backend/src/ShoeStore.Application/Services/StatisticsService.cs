using ErrorOr;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Interface.StatisticsInterface;

namespace ShoeStore.Application.Services;

/// <summary>
///     Provides statistics data for dashboard summary, chart, and top products.
/// </summary>
/// <param name="invoiceRepository">Repository used to read invoice-based statistics data.</param>
public class StatisticsService(IInvoiceRepository invoiceRepository) : IStatisticsService
{
    /// <summary>
    ///     Gets summary metrics for the current month and growth compared to the previous month.
    /// </summary>
    /// <param name="cancellationToken">Cancellation token for the async operation.</param>
    /// <returns>
    ///     A summary response including total revenue, total orders, average revenue, and growth percentages.
    /// </returns>
    public async Task<ErrorOr<StatisticsSummaryResponseDto>> GetStatisticsSummaryAsync(
        CancellationToken cancellationToken)
    {
        var currentEndDate = DateTime.UtcNow.ToVnTime();
        var currentStartDate = currentEndDate.ToFirstDayOfMonth();
        var previousStartDate = currentStartDate.AddMonths(-1);
        var previousEndDate = currentEndDate.AddMonths(-1);

        var currentMetrics =
            await invoiceRepository.GetSummaryMetricsAsync(currentStartDate, currentEndDate, cancellationToken);
        ;
        var previousMetrics =
            await invoiceRepository.GetSummaryMetricsAsync(previousStartDate, previousEndDate, cancellationToken);

        var previousAverageRevenue = previousMetrics.TotalInvoices > 0
            ? previousMetrics.TotalRevenue / previousMetrics.TotalInvoices
            : 0;
        var currentAverageRevenue = currentMetrics.TotalInvoices > 0
            ? currentMetrics.TotalRevenue / currentMetrics.TotalInvoices
            : 0;

        var growthTotalRevenue = CalculateGrowthTotalRevenue(previousMetrics.TotalRevenue, currentMetrics.TotalRevenue);
        var growthTotalInvoice =
            CalculateGrowthTotalInvoice(previousMetrics.TotalInvoices, currentMetrics.TotalInvoices);
        var growthAverageRevenue = CalculateGrowthAverageRevenue(previousAverageRevenue, currentAverageRevenue);

        return new StatisticsSummaryResponseDto(
            currentMetrics.TotalRevenue,
            currentMetrics.TotalInvoices,
            currentAverageRevenue,
            growthTotalInvoice,
            growthTotalRevenue,
            growthAverageRevenue
        );
    }

    /// <summary>
    ///     Gets chart data by period type.
    /// </summary>
    /// <remarks>
    ///     Supported period types:
    ///     - <c>week</c> (default): Last 7 days, grouped by day. Shows daily revenue trend for the current week.
    ///     - <c>30days</c>: Last 30 days, grouped by day. Shows daily revenue trend for the past month.
    ///     - <c>12months</c>: Last 12 months, grouped by month. Shows monthly revenue trend for the past year.
    /// </remarks>
    /// <param name="type">
    ///     Period type filter. Use <c>week</c>, <c>30days</c>, or <c>12months</c>. Defaults to <c>week</c> if
    ///     invalid.
    /// </param>
    /// <param name="token">Cancellation token for the async operation.</param>
    /// <returns>A chart response containing time-based revenue data points.</returns>
    public async Task<ErrorOr<StatisticsChartResponseDto>> GetStatisticsChartAsync(string type,
        CancellationToken token)
    {
        var currentEndDate = DateTime.UtcNow.ToVnTime();
        DateTime startDate;
        string groupByType;
        switch (type.Trim().ToLower())
        {
            default:
                startDate = currentEndDate.AddDays(-7);
                groupByType = "day";
                break;
            case "30days":
                startDate = currentEndDate.AddMonths(-1);
                groupByType = "day";
                break;
            case "12months":
                startDate = currentEndDate.AddYears(-1);
                groupByType = "month";
                break;
        }

        var rawData = await invoiceRepository.GetChartDataAsync(startDate, currentEndDate, groupByType, token);

        var chartData = GetDataChartByType(rawData, startDate, currentEndDate, groupByType);
        return new StatisticsChartResponseDto(chartData);
    }

    /// <summary>
    ///     Gets top-selling products of the current month and revenue growth versus the previous month.
    /// </summary>
    /// <param name="cancellationToken">Cancellation token for the async operation.</param>
    /// <returns>A list of top product statistics with sales, revenue, and growth percentage.</returns>
    public async Task<ErrorOr<List<ProductHighestStatisticsResponseDto>>> GetProductsHighestStatisticsAsync(
        CancellationToken cancellationToken)
    {
        var currentEndDate = DateTime.UtcNow.ToVnTime();
        var currentStartDate = currentEndDate.ToFirstDayOfMonth();
        var previousStartDate = currentStartDate.AddMonths(-1);
        var previousEndDate = currentEndDate.AddMonths(-1);
        var currentTop3Product =
            await invoiceRepository.GetTop3ProductsAsync(currentStartDate, currentEndDate, [], cancellationToken);

        var productIds = currentTop3Product.Select(product => product.ProductPublicId).ToList();

        var previousTop3Product =
            await invoiceRepository.GetTop3ProductsAsync(previousStartDate, previousEndDate, productIds,
                cancellationToken);

        var result = new List<ProductHighestStatisticsResponseDto>();
        foreach (var product in currentTop3Product)
        {
            var matchVariant = previousTop3Product.FirstOrDefault(p => p.ProductPublicId == product.ProductPublicId);
            var previousRevenue = matchVariant?.TotalRevenue ?? 0;
            var growthRevenue = previousRevenue > 0
                ? CalculateGrowthTotalRevenue(previousRevenue, product.TotalRevenue)
                : 100;
            var response = new ProductHighestStatisticsResponseDto(
                product.ProductPublicId,
                product.ProductName,
                product.ImageUrl ?? string.Empty,
                product.TotalInvoices,
                product.TotalRevenue,
                growthRevenue);
            result.Add(response);
        }

        return result;
    }

    private static decimal CalculateGrowthTotalRevenue(decimal previousRevenue, decimal currentRevenue)
    {
        if (previousRevenue == 0) return 100; // Avoid division by zero
        return (currentRevenue - previousRevenue) / previousRevenue * 100;
    }

    private static decimal CalculateGrowthTotalInvoice(int previousInvoiceNumber, int currentInvoiceNumber)
    {
        if (previousInvoiceNumber == 0) return 100;
        return (decimal)(currentInvoiceNumber - previousInvoiceNumber) / previousInvoiceNumber * 100;
    }

    private static decimal CalculateGrowthAverageRevenue(decimal previousAverageRevenue, decimal currentAverageRevenue)
    {
        if (previousAverageRevenue == 0) return 100; // Avoid division by zero
        return (currentAverageRevenue - previousAverageRevenue) / previousAverageRevenue * 100;
    }

    private static List<ChartDataDto> GetDataChartByType(List<(DateTime Date, decimal Revenue)> rawData,
        DateTime startDate, DateTime endDate, string groupByType)
    {
        var result = new List<ChartDataDto>();

        if (groupByType == "month")
        {
            var startMonth = new DateTime(startDate.Year, startDate.Month, 1);
            var endMonth = new DateTime(endDate.Year, endDate.Month, 1);

            for (var m = startMonth; m <= endMonth; m = m.AddMonths(1))
            {
                var match = rawData.FirstOrDefault(data => data.Date.Month == m.Month && data.Date.Year == m.Year);
                result.Add(new ChartDataDto(m.ToString("MM/yyyy"), match.Revenue));
            }
        }
        else // groupByType == "day"
        {
            for (var d = startDate.Date; d <= endDate.Date; d = d.AddDays(1))
            {
                var match = rawData.FirstOrDefault(data => data.Date.Date == d);
                result.Add(new ChartDataDto(d.ToString("dd/MM"), match.Revenue));
            }
        }

        return result;
    }
}