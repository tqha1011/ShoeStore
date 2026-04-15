using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface;

public interface ICategoryRepository : IGenericRepository<Category, int>
{
    Task<List<Category>> GetCategoriesAsync(CancellationToken token);

    Task<bool> CategoryNameExistAsync(string name, CancellationToken token);
}