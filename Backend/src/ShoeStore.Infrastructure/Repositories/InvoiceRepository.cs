using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
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
}