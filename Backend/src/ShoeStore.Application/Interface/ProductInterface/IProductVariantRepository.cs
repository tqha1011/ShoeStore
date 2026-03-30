using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.ProductInterface;

public interface IProductVariantRepository : IGenericRepository<ProductVariant, int>
{
    Task<ProductVariant?> GetByGuidAsync(Guid productGuid, CancellationToken token);
}