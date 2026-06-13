using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using ShoeStore.Application.Interface.ChatBotInterface;

namespace ShoeStore.Infrastructure.Worker;

public class SyncProductEmbeddingService(
    IProductEmbeddingQueue queue,
    ILogger<SyncProductEmbeddingService> logger,
    IServiceScopeFactory scopeFactory) : BackgroundService
{
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        logger.LogInformation("SyncProductEmbeddingService started");
        while (!stoppingToken.IsCancellationRequested)
            try
            {
                var productPublicId = await queue.DequeueAsync(stoppingToken);
                using var scope = scopeFactory.CreateScope();
                var embeddingService = scope.ServiceProvider.GetRequiredService<IProductEmbeddingService>();
                var result =
                    await embeddingService.SyncVectorEmbeddingByProductPublicId(productPublicId, stoppingToken);
                if (result.IsError)
                    logger.LogError("Failed to sync embedding for product {ProductPublicId}. Errors: {Errors}",
                        productPublicId, result.Errors);
                else
                    logger.LogInformation("Successfully synced embedding for product {ProductPublicId}",
                        productPublicId);
            }
            catch (OperationCanceledException ex)
            {
                logger.LogError(ex, "SyncProductEmbeddingService is stopping due to cancellation at {Time}",
                    DateTime.Now);
            }
            catch (Exception ex)
            {
                logger.LogError(ex, "Error syncing embedding for product in SyncProductEmbeddingService at {Time}",
                    DateTime.Now);
            }
    }
}