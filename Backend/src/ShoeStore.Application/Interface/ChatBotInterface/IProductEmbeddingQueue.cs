namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IProductEmbeddingQueue
{
    ValueTask EnqueueAsync(Guid productPublicId, CancellationToken token);

    ValueTask<Guid> DequeueAsync(CancellationToken token);
}