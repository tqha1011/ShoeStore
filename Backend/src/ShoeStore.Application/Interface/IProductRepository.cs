using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface
{
    public interface IProductRepository : IGenericRepository<Product, int>
    {
        IQueryable<Product> SearchProduct(ProductSearchRequest request);
        Task<Product?> GetByGuidAsync(Guid productGuid, CancellationToken token);
    }
}
