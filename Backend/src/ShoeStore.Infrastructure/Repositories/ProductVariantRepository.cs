using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class ProductVariantRepository(AppDbContext context)
    : GenericRepository<ProductVariant, int>(context), IProductVariantRepository
{
    public async Task<ProductVariant?> GetByGuidAsync(Guid productGuid, CancellationToken token)
    {
        return await DbSet.Include(x => x.Size)
            .Include(x => x.Color)
            .Include(x => x.Product)
            .FirstOrDefaultAsync(x => x.PublicId == productGuid, token);
    }
}