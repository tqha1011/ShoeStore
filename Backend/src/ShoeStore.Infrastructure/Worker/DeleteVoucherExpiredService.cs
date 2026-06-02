using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using ShoeStore.Application.Interface.VoucherInterface;

namespace ShoeStore.Infrastructure.Worker;

public class DeleteVoucherExpiredService(
    ILogger<DeleteVoucherExpiredService> logger,
    IServiceScopeFactory scopeFactory) : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        logger.LogInformation("Deleting Voucher Expired Service starting");
        await CleanUpVoucherExpiredAsync(stoppingToken);
        using var timer = new PeriodicTimer(TimeSpan.FromHours(24));

        while (await timer.WaitForNextTickAsync(stoppingToken)) await CleanUpVoucherExpiredAsync(stoppingToken);
    }

    private async Task CleanUpVoucherExpiredAsync(CancellationToken stoppingToken)
    {
        try
        {
            using var scope = scopeFactory.CreateScope();
            var voucherRepository = scope.ServiceProvider.GetRequiredService<IVoucherRepository>();
            var deletedCounts = await voucherRepository
                .GetAllVouchers(true)
                .Where(v => v.ValidTo < DateTime.UtcNow && !v.IsDeleted)
                .ExecuteUpdateAsync(s => s
                    .SetProperty(v => v.IsDeleted, true)
                    .SetProperty(v => v.UpdatedAt, DateTime.UtcNow), stoppingToken);
            logger.LogInformation("Deleted {DeletedCounts} expired vouchers", deletedCounts);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Error Deleting Voucher Expired Service");
        }
    }
}