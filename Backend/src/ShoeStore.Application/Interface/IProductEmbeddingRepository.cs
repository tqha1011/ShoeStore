using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities.Embedding;

namespace ShoeStore.Application.Interface;

public interface IProductEmbeddingRepository : IGenericRepository<ProductEmbedding, int>
{
    void AddRange(IEnumerable<ProductEmbedding> productEmbeddings);

    Task<List<ProductEmbedding>>
        GetTop5ProductByVectorAsync(ReadOnlyMemory<float> queryVector, CancellationToken token);
}