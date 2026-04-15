using ErrorOr;
using ShoeStore.Application.DTOs.StatisticsDto;

namespace ShoeStore.Application.Interface.StatisticsInterface;

public interface IStatisticsService
{
    Task<ErrorOr<StatisticsSummaryResponseDto>> GetStatisticsSummaryAsync(CancellationToken cancellationToken);

    Task<ErrorOr<List<ProductHighestStatisticsResponseDto>>> GetProductsHighestStatisticsAsync(
        CancellationToken cancellationToken);

    Task<ErrorOr<StatisticsChartResponseDto>> GetStatisticsChartAsync(string type,
        CancellationToken token);
}