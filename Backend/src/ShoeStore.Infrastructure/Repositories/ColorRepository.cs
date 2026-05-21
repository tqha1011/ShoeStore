using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.MasterDataInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class ColorRepository(AppDbContext context) : GenericRepository<Color, int>(context), IColorRepository
{
    public async Task<List<Color>> GetColorsAsync(CancellationToken token)
    {
        return await DbSet.AsNoTracking().ToListAsync(token);
    }

    public async Task<bool> ColorNameExistAsync(string name, CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .AnyAsync(x => x.ColorName.ToLower() == name.ToLower(), token);
    }

    public async Task<int?> GetColorIdAsync(string name, CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .Where(x => x.ColorName.Equals(name, StringComparison.CurrentCultureIgnoreCase))
            .Select(x => (int?)x.Id)
            .FirstOrDefaultAsync(token);
    }
}