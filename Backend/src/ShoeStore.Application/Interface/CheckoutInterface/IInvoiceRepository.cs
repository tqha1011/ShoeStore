using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.CheckoutInterface;

public interface IInvoiceRepository : IGenericRepository<Invoice, int>
{
    IQueryable<Invoice> GetAll();

    Task<Invoice?> GetInvoiceDetailIdAsync(Guid publicId, CancellationToken token);

    Task<Invoice?> GetInvoiceByOrderCodeAsync(string orderCode, CancellationToken token);

    Task<(int TotalInvoices, decimal TotalRevenue)> GetSummaryMetricsAsync(DateTime startDate, DateTime endDate,
        CancellationToken token);

    Task<List<ProductHighestStatisticsDto>> GetTop3VariantsAsync(DateTime startDate, DateTime endDate,
        List<int> variantIds, CancellationToken token);

    Task<List<(DateTime Date, decimal Revenue)>> GetChartDataAsync(DateTime startDate, DateTime endDate, string type,
        CancellationToken token);
}