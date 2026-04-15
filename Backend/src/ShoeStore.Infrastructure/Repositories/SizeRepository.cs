using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.MasterDataInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class SizeRepository(AppDbContext context) : GenericRepository<ProductSize, int>(context), ISizeRepository
{
    public async Task<List<ProductSize>> GetProductSizesAsync(CancellationToken token)
    {
        return await DbSet.AsNoTracking().ToListAsync(token);
    }

    public Task<bool> ProductSizeExistsAsync(decimal size, CancellationToken token)
    {
        return DbSet.AsNoTracking()
            .AnyAsync(x => x.Size == size, token);
    }
}