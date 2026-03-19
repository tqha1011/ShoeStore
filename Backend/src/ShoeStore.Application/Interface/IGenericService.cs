namespace ShoeStore.Application.Interface
{
    internal interface IGenericService<TEntity> where TEntity : class 
    {
        // Create
        Task<TEntity> AddAsync(TEntity entity);
        // Read
        Task<IEnumerable<TEntity>> GetAllAsync();
        Task<TEntity?> GetByIdAsync(Guid id);
        // Update
        Task UpdateAsync(TEntity entity);
        // Delete
        Task DeleteAsync(Guid id);
    }
}
