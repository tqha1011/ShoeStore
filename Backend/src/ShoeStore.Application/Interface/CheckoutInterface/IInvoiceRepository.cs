using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.CheckoutInterface;

public interface IInvoiceRepository : IGenericRepository<Invoice, int>
{
    IQueryable<Invoice> GetAll();

    Task<Invoice?> GetInvoiceDetailIdAsync(Guid publicId, CancellationToken token);

    Task<Invoice?> GetInvoiceByOrderCodeAsync(string orderCode, CancellationToken token);

    Task<List<Invoice>> GetInvoicesByDateAsync(DateTime startDate, DateTime endDate, CancellationToken token);

    Task<List<ProductHighestStatisticsDto>> GetTop3VariantsAsync(DateTime startDate, DateTime endDate,
        List<int> variantIds, CancellationToken token);
}