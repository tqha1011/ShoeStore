using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class SizeRepository(AppDbContext context) : GenericRepository<ProductSize, int>(context), ISizeRepository
{
    public async Task<List<ProductSize>> GetProductSizesAsync(CancellationToken token)
    {
        return await DbSet.AsNoTracking().ToListAsync(token);
    }
}