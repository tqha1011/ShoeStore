using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;


namespace ShoeStore.Infrastructure.Repositories
{
    public class ProductVariantRepository(AppDbContext context) : GenericRepository<ProductVariant, int>(context), IProductVariantRepository
    {
        public async Task<ProductVariant?> GetByGuidAsync(Guid productGuid, CancellationToken token)
        {
            return await context.ProductVariants.AsNoTracking().FirstOrDefaultAsync(x => x.PublicId == productGuid, token);
        }
    }
}
