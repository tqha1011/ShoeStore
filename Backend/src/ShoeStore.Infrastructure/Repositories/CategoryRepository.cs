using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class CategoryRepository(AppDbContext context) : GenericRepository<Category, int>(context), ICategoryRepository
{
    public async Task<List<Category>> GetCategoriesAsync(CancellationToken token)
    {
        return await DbSet.AsNoTracking().ToListAsync(token);
    }

    public async Task<bool> CategoryNameExistAsync(string name, CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .AnyAsync(x => x.Name.ToLower() == name.ToLower(), token);
    }
}