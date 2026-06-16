using System.Threading.Channels;
using ShoeStore.Application.Interface.ChatBotInterface;

namespace ShoeStore.Application.Services;

public class SyncEmbeddingService : IProductEmbeddingQueue
{
    private readonly Channel<Guid> _channel;

    public SyncEmbeddingService()
    {
        var option = new BoundedChannelOptions(15)
        {
            FullMode = BoundedChannelFullMode.Wait
        };
        _channel = Channel.CreateBounded<Guid>(option);
    }

    public async ValueTask EnqueueAsync(Guid productPublicId, CancellationToken token)
    {
        await _channel.Writer.WriteAsync(productPublicId, token);
    }

    public async ValueTask<Guid> DequeueAsync(CancellationToken token)
    {
        return await _channel.Reader.ReadAsync(token);
    }
}