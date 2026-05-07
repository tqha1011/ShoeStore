using ErrorOr;

namespace ShoeStore.Application.Interface;

public interface IProductEmbeddingService
{
    Task<ErrorOr<Created>> GenerateVectorEmbeddingWithExistDataAsync(CancellationToken cancellationToken);

    Task<ErrorOr<Created>> GenerateVectorEmbeddingByProductPublicId(Guid productId, CancellationToken cancellationToken);
}