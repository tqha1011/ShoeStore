using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface.StatisticsInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Provides read-only statistics endpoints for dashboard and reporting screens.
/// </summary>
/// <param name="statisticsService">Service that aggregates statistics data from invoices and orders.</param>
[Route("api/statistics")]
[ApiController]
[Authorize(Roles = "Admin")]
[EnableRateLimiting("limit-per-user")]
public class StatisticsController(IStatisticsService statisticsService) : ControllerBase
{
    /// <summary>
    ///     Gets summary metrics for the current month compared with the previous month.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization.
    /// </remarks>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Summary statistics were returned successfully.</response>
    /// <response code="500">An unexpected server error occurred.</response>
    [ProducesResponseType(typeof(StatisticsSummaryResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("summary")]
    public async Task<IActionResult> GetSummary(CancellationToken token)
    {
        var result = await statisticsService.GetStatisticsSummaryAsync(token);
        var response = result.Match<IActionResult>(
            data => Ok(data),
            errors => StatusCode(StatusCodes.Status500InternalServerError, new
            {
                message = "Failed to load summary statistics",
                detail = errors[0].Description
            })
        );
        return response;
    }

    /// <summary>
    ///     Gets top-selling products in the current month with growth compared to the previous month.
    /// </summary>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Top products statistics were returned successfully.</response>
    /// <response code="500">An unexpected server error occurred.</response>
    [ProducesResponseType(typeof(List<ProductHighestStatisticsResponseDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("top-products")]
    public async Task<IActionResult> GetTopProducts(CancellationToken token)
    {
        var result = await statisticsService.GetProductsHighestStatisticsAsync(token);
        var response = result.Match<IActionResult>(
            data => Ok(data),
            errors => StatusCode(StatusCodes.Status500InternalServerError, new
            {
                message = "Failed to load top products statistics",
                detail = errors[0].Description
            })
        );

        return response;
    }

    /// <summary>
    ///     Gets revenue chart data by period type.
    /// </summary>
    /// <remarks>
    ///     Use <c>type</c> query parameter: <c>7days</c> (default), <c>30days</c>, or <c>12months</c>.
    /// </remarks>
    /// <param name="type">Chart period type.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Chart statistics were returned successfully.</response>
    /// <response code="500">An unexpected server error occurred.</response>
    [ProducesResponseType(typeof(StatisticsChartResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("chart")]
    public async Task<IActionResult> GetChart([FromQuery] string? type, CancellationToken token)
    {
        var safeTimeRange = string.IsNullOrWhiteSpace(type) ? "7days" : type;
        var result = await statisticsService.GetStatisticsChartAsync(safeTimeRange, token);
        var response = result.Match<IActionResult>(
            data => Ok(data),
            errors => StatusCode(StatusCodes.Status500InternalServerError, new
            {
                message = "Failed to load chart statistics",
                detail = errors[0].Description
            })
        );

        return response;
    }
}