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
            try
            {
                using var scope = serviceScopeFactory.CreateScope();
                var dbContext = scope.ServiceProvider.GetRequiredService<AppDbContext>();

                var expiredTime = DateTime.UtcNow.AddMinutes(-15);

                var expiredInvoices = await dbContext.Invoices
                    .Include(iv => iv.InvoiceDetails)
                    .ThenInclude(dt => dt.ProductVariant)
                    .Where(iv => iv.Status == InvoiceStatus.Pending && iv.CreatedAt <= expiredTime)
                    .ToListAsync(stoppingToken);

                if (expiredInvoices.Count != 0)
                    foreach (var invoice in expiredInvoices)
                    {
                        invoice.Status = InvoiceStatus.Cancelled;

                        foreach (var detail in invoice.InvoiceDetails)
                        {
                            //detail.ProductVariant?.Stock += detail.Quantity;
                        }

                        logger.LogInformation("Invoice {InvoiceId} canceled", invoice.Id);
                    }

                await dbContext.SaveChangesAsync(stoppingToken);
            }
            catch (Exception ex)
            {
                logger.LogError(ex, "OrderCancellationService failed");
            }
    }
}