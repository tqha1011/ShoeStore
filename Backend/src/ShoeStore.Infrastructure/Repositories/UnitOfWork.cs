using ShoeStore.Application.Interface;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class UnitOfWork(AppDbContext context) : IUnitOfWork
{
    public async Task SaveChangesAsync(CancellationToken token = default)
    {
        await context.SaveChangesAsync(token);
    }
}