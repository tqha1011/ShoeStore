using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface;

public interface IInvoiceRepository : IGenericRepository<Invoice, int>
{
    IQueryable<Invoice> GetAll();

    Task<Invoice?> GetInvoiceDetailIdAsync(Guid publicId, CancellationToken token);
}