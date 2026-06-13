using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities.Embedding;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IProductEmbeddingRepository : IGenericRepository<ProductEmbedding, int>
{
    void AddRange(IEnumerable<ProductEmbedding> productEmbeddings);

    Task<ProductEmbedding?> GetByProductIdAsync(int productId, CancellationToken token);

    Task<ProductEmbedding?> GetByProductPublicIdAsync(Guid productPublicId, CancellationToken token);

    Task<List<ProductEmbedding>>
        GetTop5ProductByVectorAsync(ReadOnlyMemory<float> queryVector, CancellationToken token);
}
