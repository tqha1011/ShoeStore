using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;


namespace ShoeStore.Infrastructure.Repositories
{
    public class ProductVariantRepository(AppDbContext context) : GenericRepository<ProductVariant, int>(context), IProductVariantRepository
    {
        public void Add(ProductVariant entity)
        {
            context.Entry(entity.Product).State = EntityState.Unchanged; 
            context.ProductVariants.Add(entity);
        }

        public void Delete(ProductVariant entity)
        {
            context.ProductVariants.Remove(entity);
        }

        public async Task<ProductVariant?> GetByIdAsync(int id, CancellationToken token)
        {
            return await context.ProductVariants.AsNoTracking().FirstOrDefaultAsync(x => x.Id!.Equals(id), token);
        }

        public void Update(ProductVariant entity)
        {
            context.ProductVariants.Update(entity);
        }

        public async Task<ProductVariant?> GetByGuidAsync(Guid productGuid, CancellationToken token)
        {
            return await context.ProductVariants.AsNoTracking().FirstOrDefaultAsync(x => x.PublicId == productGuid, token);
        }
    }
}
