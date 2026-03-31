using ShoeStore.Domain.Common;

namespace ShoeStore.Application.Interface.Common;

public interface IGenericRepository<TEntity,TEntityId> 
    where TEntity : Entity<TEntityId> 
{
    void Add(TEntity entity);
    void Update(TEntity entity);
    void Delete(TEntity entity);
    Task<TEntity?> GetByIdAsync(TEntityId id,CancellationToken token);
}