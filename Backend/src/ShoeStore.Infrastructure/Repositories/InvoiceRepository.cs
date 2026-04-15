using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface.CheckoutInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;
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

    public async Task<List<Invoice>> GetInvoicesByDateAsync(DateTime startDate, DateTime endDate,
        CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .Where(inv => inv.CreatedAt >= startDate && inv.CreatedAt <= endDate && inv.Status == InvoiceStatus.Paid)
            .ToListAsync(token);
    }

    public async Task<List<ProductHighestStatisticsDto>> GetTop3VariantsAsync(DateTime startDate, DateTime endDate,
        List<int> variantIds,
        CancellationToken token)
    {
        var query = DbSet.AsNoTracking().Where(inv =>
                inv.CreatedAt >= startDate && inv.CreatedAt <= endDate && inv.Status == InvoiceStatus.Paid)
            .SelectMany(inv => inv.InvoiceDetails);
        if (variantIds.Count != 0) query = query.Where(invDet => variantIds.Contains(invDet.ProductVariantId));
        return await query.GroupBy(x => new
            {
                x.ProductVariantId, x.ProductVariant!.Product.PublicId, x.ProductVariant!.Product.ProductName,
                x.ProductVariant!.ImageUrl
            })
            .Select(p => new ProductHighestStatisticsDto(
                p.Key.PublicId,
                p.Key.ProductVariantId,
                p.Key.ProductName,
                p.Key.ImageUrl,
                p.Select(invDet => invDet.Invoice!.Id).Distinct().Count(),
                p.Sum(invDet => invDet.UnitPrice * invDet.Quantity)))
            .OrderByDescending(p => p.TotalRevenue)
            .Take(3)
            .ToListAsync(token);
    }
}