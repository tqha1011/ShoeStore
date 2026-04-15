using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface.InvoiceInterface;
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

    public async Task<List<ProductHighestStatisticsDto>> GetTop3ProductsAsync(DateTime startDate, DateTime endDate,
        List<Guid> productIds,
        CancellationToken token)
    {
        var utcStartDate = DateTime.SpecifyKind(startDate, DateTimeKind.Utc);
        var utcEndDate = DateTime.SpecifyKind(endDate, DateTimeKind.Utc);
        var query = DbSet.AsNoTracking().Where(inv =>
                inv.CreatedAt >= utcStartDate && inv.CreatedAt <= utcEndDate && inv.Status == InvoiceStatus.Paid)
            .SelectMany(inv => inv.InvoiceDetails)
            .Select(ivDet => new
            {
                ivDet.ProductVariantId,
                ProductPublicId = ivDet.ProductVariant!.Product.PublicId,
                ivDet.ProductVariant!.Product.ProductName,
                ImgUrl = ivDet.ProductVariant!.ImageUrl,
                ivDet.InvoiceId,
                LineTotal = ivDet.Quantity * ivDet.UnitPrice
            });

        if (productIds.Count != 0) query = query.Where(invDet => productIds.Contains(invDet.ProductPublicId));

        var topVariants = query.GroupBy(x => new
            {
                x.ProductPublicId, x.ProductName
            })
            .Select(p => new
            {
                p.Key.ProductPublicId,
                p.Key.ProductName,
                ImageUrl = p.Max(x => x.ImgUrl),
                TotalInvoies = p.Select(invDet => invDet.InvoiceId).Distinct().Count(),
                TotalRevenue = p.Sum(invDet => invDet.LineTotal)
            })
            .OrderByDescending(p => p.TotalRevenue)
            .Take(3);

        var rawData = await topVariants.ToListAsync(token);

        return rawData.Select(p => new ProductHighestStatisticsDto(
            p.ProductPublicId,
            p.ProductName,
            p.ImageUrl,
            p.TotalInvoies,
            p.TotalRevenue)).ToList();
    }

    public async Task<List<(DateTime Date, decimal Revenue)>> GetChartDataAsync(DateTime startDate, DateTime endDate,
        string groupByType, CancellationToken token)
    {
        var utcStartDate = DateTime.SpecifyKind(startDate, DateTimeKind.Utc);
        var utcEndDate = DateTime.SpecifyKind(endDate, DateTimeKind.Utc);
        var query = DbSet.AsNoTracking().Where(inv =>
            inv.CreatedAt >= utcStartDate && inv.CreatedAt <= utcEndDate && inv.Status == InvoiceStatus.Paid);

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
        var utcStartDate = DateTime.SpecifyKind(startDate, DateTimeKind.Utc);
        var utcEndDate = DateTime.SpecifyKind(endDate, DateTimeKind.Utc);
        var metrics = await DbSet.AsNoTracking()
            .Where(inv => inv.CreatedAt >= utcStartDate && inv.CreatedAt <= utcEndDate &&
                          inv.Status == InvoiceStatus.Paid)
            .GroupBy(inv => 1)
            .Select(g => new
            {
                Count = g.Count(),
                Revenue = g.Sum(inv => inv.FinalPrice)
            })
            .FirstOrDefaultAsync(token);
        return metrics != null ? (metrics.Count, metrics.Revenue) : (0, 0m);
    }

    public IQueryable<InvoiceDetail> GetInvoiceDetail(Guid invoiceGuid)
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