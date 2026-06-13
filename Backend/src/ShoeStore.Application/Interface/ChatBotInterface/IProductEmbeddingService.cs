using ErrorOr;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IProductEmbeddingService
{
    Task<ErrorOr<Created>> GenerateVectorEmbeddingWithExistDataAsync(CancellationToken cancellationToken);

    Task<ErrorOr<Created>>
        GenerateVectorEmbeddingByProductPublicId(Guid productId, CancellationToken cancellationToken);

    Task<ErrorOr<Success>>
        SyncVectorEmbeddingByProductPublicId(Guid productId, CancellationToken cancellationToken);
}
