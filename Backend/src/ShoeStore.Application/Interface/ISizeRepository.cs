using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface;

public interface ISizeRepository : IGenericRepository<ProductSize, int>
{
    Task<List<ProductSize>> GetProductSizesAsync(CancellationToken token);
}