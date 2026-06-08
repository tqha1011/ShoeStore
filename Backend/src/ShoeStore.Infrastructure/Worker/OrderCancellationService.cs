using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using ShoeStore.Domain.Enum;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Worker;

public class OrderCancellationService(
    IServiceScopeFactory serviceScopeFactory,
    ILogger<OrderCancellationService> logger) : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        logger.LogInformation("OrderCancellationService started");
        using var timer = new PeriodicTimer(TimeSpan.FromMinutes(1));

        while (await timer.WaitForNextTickAsync(stoppingToken))
        {
            using var scope = serviceScopeFactory.CreateScope();
            var dbContext = scope.ServiceProvider.GetRequiredService<AppDbContext>();
            using var transaction = await dbContext.Database.BeginTransactionAsync(stoppingToken);
            try
            {
                var expiredTime = DateTime.UtcNow.AddMinutes(-15);
                const int sePayId = (int)PaymentMethod.SePay;

                var expiredInvoices = await dbContext.Invoices
                    .Include(iv => iv.VoucherDetails)
                    .Include(iv => iv.InvoiceDetails)
                    .ThenInclude(dt => dt.ProductVariant)
                    .Where(iv =>
                        iv.Status == InvoiceStatus.Pending && iv.CreatedAt <= expiredTime &&
                        iv.PaymentId == sePayId)
                    .Take(100)
                    .ToListAsync(stoppingToken);

                if (expiredInvoices.Count != 0)
                    foreach (var invoice in expiredInvoices)
                    {
                        invoice.Status = InvoiceStatus.Cancelled;

                        foreach (var detail in invoice.InvoiceDetails)
                            if (detail.ProductVariant != null)
                                detail.ProductVariant.Stock += detail.Quantity;

                        var voucherIds = invoice.VoucherDetails.Select(vd => vd.VoucherId).ToList();
                        dbContext.UserVouchers.Where(uv =>
                                voucherIds.Contains(uv.VoucherId) && uv.UserId == invoice.UserId)
                            .ToList()
                            .ForEach(uv => { uv.ReservedCount = Math.Max(0, uv.ReservedCount - 1); });

                        logger.LogInformation("Invoice {InvoiceId} canceled", invoice.Id);
                    }

                await dbContext.SaveChangesAsync(stoppingToken);
                await transaction.CommitAsync(stoppingToken);
            }
            catch (Exception ex)
            {
                await transaction.RollbackAsync(stoppingToken);
                logger.LogError(ex, "OrderCancellationService failed");
            }
        }
    }
}