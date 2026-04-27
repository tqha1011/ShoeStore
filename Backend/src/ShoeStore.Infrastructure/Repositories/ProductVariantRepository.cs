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
        return await DbSet.Where(v => !v.IsDeleted)
            .Include(x => x.Size)
            .Include(x => x.Color)
            .Include(x => x.Product)
            .FirstOrDefaultAsync(x => x.PublicId == productGuid, token);
    }

    public async Task<List<ProductVariant>> GetListVariantsAsync(List<Guid> productVariantIds, CancellationToken token)
    {
        var variantLists = await DbSet.Where(x => !x.IsDeleted && productVariantIds.Contains(x.PublicId))
            .Distinct()
            .Include(x => x.Size)
            .Include(x => x.Color)
            .Include(x => x.Product)
            .ToListAsync(token);
        return variantLists;
    }
}