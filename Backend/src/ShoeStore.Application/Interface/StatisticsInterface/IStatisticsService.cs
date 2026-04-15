using ErrorOr;
using ShoeStore.Application.DTOs.StatisticsDto;

namespace ShoeStore.Application.Interface.StatisticsInterface;

public interface IStatisticsService
{
    Task<StatisticsSummaryResponseDto> GetStatisticsSummaryAsync(CancellationToken cancellationToken);

    Task<ErrorOr<List<ProductHighestStatisticsResponseDto>>> GetProductsHighestStatisticsAsync(
        CancellationToken cancellationToken);

    Task<ErrorOr<StatisticsChartResponseDto>> GetStatisticsChartAsync(DateTime startDate, DateTime endDate,
        CancellationToken token);
}