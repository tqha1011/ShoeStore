using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities.Embedding;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class ProductEmbeddingRepository(AppDbContext context) : GenericRepository<ProductEmbedding,int>(context),IProductEmbeddingRepository
{
    public void AddRange(IEnumerable<ProductEmbedding> productEmbeddings)
    {
        DbSet.AddRange(productEmbeddings);
    }
}