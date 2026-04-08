using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class InvoiceRepository(AppDbContext context) : GenericRepository<Invoice, int>(context), IInvoiceRepository
{
    public IQueryable<Invoice> GetAll()
    {
        return DbSet.AsQueryable();
    }

    public async Task<Invoice?> GetInvoiceDetailIdAsync(Guid publicId, CancellationToken token)
    {
        return await DbSet.Where(inv => inv.PublicId == publicId)
            .Include(inv => inv.InvoiceDetails)
            .ThenInclude(invDet => invDet.ProductVariant)
            .FirstOrDefaultAsync(token);
    }

    public Task<Invoice?> GetInvoiceByOrderCodeAsync(string orderCode, CancellationToken token)
    {
        return DbSet.Where(inv => inv.OrderCode == orderCode)
            .Include(inv => inv.PaymentTransactions)
            .FirstOrDefaultAsync(token);
    }

    public IQueryable<InvoiceDetail> GetaInvoiceDetail(Guid invoiceGuid)
    {
        return DbSet.Where(inv => inv.PublicId == invoiceGuid)
            .SelectMany(inv => inv.InvoiceDetails)
            .AsQueryable();
    }

    public async Task<Invoice?> GetByPublicIdAsync(Guid publicId, CancellationToken token)
    {
        return await DbSet.Where(inv => inv.PublicId == publicId)
            .FirstOrDefaultAsync(token);
    }
}
