using Microsoft.EntityFrameworkCore;
using Pgvector.EntityFrameworkCore;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Domain.Entities.Embedding;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class ProductEmbeddingRepository(AppDbContext context)
    : GenericRepository<ProductEmbedding, int>(context), IProductEmbeddingRepository
{
    public void AddRange(IEnumerable<ProductEmbedding> productEmbeddings)
    {
        DbSet.AddRange(productEmbeddings);
    }

    public async Task<List<ProductEmbedding>> GetTop5ProductByVectorAsync(ReadOnlyMemory<float> queryVector,
        CancellationToken token)
    {
        return await DbSet
            .AsNoTracking()
            .OrderBy(x => x.Embedding.CosineDistance(queryVector))
            .Take(5)
            .ToListAsync(token);
    }
}