using Microsoft.EntityFrameworkCore;
using Pgvector;
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

    public async Task<ProductEmbedding?> GetByProductIdAsync(int productId, CancellationToken token)
    {
        return await DbSet.FirstOrDefaultAsync(x => x.ProductId == productId, token);
    }

    public async Task<ProductEmbedding?> GetByProductPublicIdAsync(Guid productPublicId, CancellationToken token)
    {
        return await DbSet
            .Include(x => x.Product)
            .FirstOrDefaultAsync(x => x.Product != null && x.Product.PublicId == productPublicId, token);
    }

    public async Task<List<ProductEmbedding>> GetTop5ProductByVectorAsync(ReadOnlyMemory<float> queryVector,
        CancellationToken token)
    {
        return await DbSet
            .AsNoTracking()
            .OrderBy(x => x.Embedding.CosineDistance(new Vector(queryVector.ToArray())))
            .Take(5)
            .ToListAsync(token);
    }
}
