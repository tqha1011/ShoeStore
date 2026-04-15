using ErrorOr;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.CheckoutInterface;
using ShoeStore.Application.Interface.StatisticsInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class StatisticsService(IInvoiceRepository invoiceRepository) : IStatisticsService
{
    public async Task<StatisticsSummaryResponseDto> GetStatisticsSummaryAsync(CancellationToken cancellationToken)
    {
        var currentEndDate = DateTime.UtcNow.ToVnTime();
        var currentStartDate = currentEndDate.ToFirstDayOfMonth();
        var previousStartDate = currentStartDate.AddMonths(-1);
        var previousEndDate = currentEndDate.AddMonths(-1);
        var invoices =
            await invoiceRepository.GetInvoicesByDateAsync(previousStartDate, currentEndDate, cancellationToken);
        // calculate revenue
        var previousTotalRevenue = CalculateTotalRevenue(invoices, previousStartDate, previousEndDate);
        var currentTotalRevenue = CalculateTotalRevenue(invoices, currentStartDate, currentEndDate);

        // calculate total invoices
        var previousTotalInvoices = CalculateTotalInvoices(invoices, previousStartDate, previousEndDate);
        var currentTotalInvoices = CalculateTotalInvoices(invoices, currentStartDate, currentEndDate);

        // calculate average revenue
        var previousAverageRevenue = previousTotalInvoices > 0 ? previousTotalRevenue / previousTotalInvoices : 0;
        var currentAverageRevenue = currentTotalInvoices > 0 ? currentTotalRevenue / currentTotalInvoices : 0;

        // calculate growth total revenue percentage
        var growthTotalRevenue = CalculateGrowthTotalRevenue(previousTotalRevenue, currentTotalRevenue);

        // calculate growth total invoice percentage
        var growthTotalInvoice = CalculateGrowthTotalInvoice(previousTotalInvoices, currentTotalInvoices);

        // calculate growth average revenue percentage
        var growthAverageRevenue = CalculateGrowthAverageRevenue(previousAverageRevenue, currentAverageRevenue);
        var response = new StatisticsSummaryResponseDto
        (
            currentTotalRevenue,
            currentTotalInvoices,
            currentAverageRevenue,
            growthTotalInvoice,
            growthTotalRevenue,
            growthAverageRevenue
        );
        return response;
    }

    public Task<ErrorOr<StatisticsChartResponseDto>> GetStatisticsChartAsync(DateTime startDate, DateTime endDate,
        CancellationToken token)
    {
        throw new NotImplementedException();
    }

    public async Task<ErrorOr<List<ProductHighestStatisticsResponseDto>>> GetProductsHighestStatisticsAsync(
        CancellationToken cancellationToken)
    {
        var currentEndDate = DateTime.UtcNow.ToVnTime();
        var currentStartDate = currentEndDate.ToFirstDayOfMonth();
        var previousStartDate = currentStartDate.AddMonths(-1);
        var previousEndDate = currentEndDate.AddMonths(-1);
        var currentTop3Product =
            await invoiceRepository.GetTop3VariantsAsync(currentStartDate, currentEndDate, [], cancellationToken);

        var variantIds = currentTop3Product.Select(variant => variant.VariantId).ToList();

        var previousTop3Product =
            await invoiceRepository.GetTop3VariantsAsync(previousStartDate, previousEndDate, variantIds,
                cancellationToken);

        var result = new List<ProductHighestStatisticsResponseDto>();
        foreach (var product in currentTop3Product)
        {
            var matchVariant = previousTop3Product.FirstOrDefault(p => p.VariantId == product.VariantId);
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

    private static decimal CalculateTotalRevenue(List<Invoice> invoices, DateTime startDate, DateTime endDate)
    {
        return invoices.Where(inv => inv.CreatedAt >= startDate && inv.CreatedAt <= endDate)
            .Sum(inv => inv.FinalPrice);
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

    private static int CalculateTotalInvoices(List<Invoice> invoices, DateTime startDate, DateTime endDate)
    {
        return invoices.Count(inv => inv.CreatedAt >= startDate && inv.CreatedAt <= endDate);
    }

    private static decimal CalculateGrowthAverageRevenue(decimal previousAverageRevenue, decimal currentAverageRevenue)
    {
        if (previousAverageRevenue == 0) return 100; // Avoid division by zero
        return (currentAverageRevenue - previousAverageRevenue) / previousAverageRevenue * 100;
    }
}