using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Common;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public abstract class GenericRepository<TEntity, TEntityId>(AppDbContext context)
    : IGenericRepository<TEntity, TEntityId>
    where TEntity : Entity<TEntityId>
{
    protected readonly DbSet<TEntity> DbSet = context.Set<TEntity>();

    public void Add(TEntity entity)
    {
        DbSet.Add(entity);
    }

    public void Update(TEntity entity)
    {
        DbSet.Update(entity);
    }

    public void Delete(TEntity entity)
    {
        DbSet.Remove(entity);
    }

    public async Task<TEntity?> GetByIdAsync(TEntityId id,CancellationToken token)
    { 
        return await DbSet.AsNoTracking().FirstOrDefaultAsync(x => x.Id!.Equals(id), token);
    }
}