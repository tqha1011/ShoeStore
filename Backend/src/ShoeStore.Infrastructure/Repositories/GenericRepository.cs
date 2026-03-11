using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Common;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class GenericRepository<TEntity,TEntityId>(AppDbContext context) : IGenericRepository<TEntity,TEntityId> 
    where TEntity : Entity<TEntityId>
{
    public void Add(TEntity entity)
    {
        context.Set<TEntity>().Add(entity);
    }

    public void Update(TEntity entity)
    {
        context.Set<TEntity>().Update(entity);
    }

    public void Delete(TEntity entity)
    {
        context.Set<TEntity>().Remove(entity);
    }

    public async Task<TEntity?> GetByIdAsync(TEntityId id,CancellationToken token)
    { 
        return await context.Set<TEntity>().AsNoTracking().FirstOrDefaultAsync(x => x.Id!.Equals(id), token);
    }
}