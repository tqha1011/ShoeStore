using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface.InvoiceInterface;
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

    public async Task<List<(DateTime Date, decimal Revenue)>> GetChartDataAsync(DateTime startDate, DateTime endDate,
        string groupByType, CancellationToken token)
    {
        var query = DbSet.AsNoTracking().Where(inv =>
            inv.CreatedAt >= startDate && inv.CreatedAt <= endDate && inv.Status == InvoiceStatus.Paid);

        if (groupByType == "month")
        {
            var rawMonthData = await query.GroupBy(inv => new { inv.CreatedAt.Year, inv.CreatedAt.Month })
                .Select(iv => new { iv.Key.Year, iv.Key.Month, Revenue = iv.Sum(inv => inv.FinalPrice) })
                .ToListAsync(token);

            return rawMonthData
                .Select(iv => new ValueTuple<DateTime, decimal>(new DateTime(iv.Year, iv.Month, 1), iv.Revenue))
                .ToList();
        }

        // default group by day
        var rawDayData = await query.GroupBy(inv => inv.CreatedAt.Date)
            .Select(iv => new { Date = iv.Key, Revenue = iv.Sum(inv => inv.FinalPrice) })
            .ToListAsync(token);

        return rawDayData.Select(iv => new ValueTuple<DateTime, decimal>(iv.Date, iv.Revenue)).ToList();
    }

    public async Task<(int TotalInvoices, decimal TotalRevenue)> GetSummaryMetricsAsync(DateTime startDate,
        DateTime endDate,
        CancellationToken token)
    {
        var metrics = await DbSet.AsNoTracking()
            .Where(inv => inv.CreatedAt >= startDate && inv.CreatedAt <= endDate && inv.Status == InvoiceStatus.Paid)
            .GroupBy(inv => 1)
            .Select(g => new
            {
                Count = g.Count(),
                Revenue = g.Sum(inv => inv.FinalPrice)
            })
            .FirstOrDefaultAsync(token);
        return metrics != null ? (metrics.Count, metrics.Revenue) : (0, 0m);
    }
}
    public IQueryable<InvoiceDetail> GetaInvoiceDetail(Guid invoiceGuid)
    {
        return DbSet.Where(inv => inv.PublicId == invoiceGuid)
            .SelectMany(inv => inv.InvoiceDetails)
            .AsQueryable();
    }

    public async Task<Invoice?> GetByPublicIdAsync(Guid publicId, CancellationToken token)
    {
        return await DbSet.Where(inv => inv.PublicId == publicId).Include(inv => inv.User)
            .FirstOrDefaultAsync(token);
    }
}
