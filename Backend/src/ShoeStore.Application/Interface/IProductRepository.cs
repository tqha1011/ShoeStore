using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface
{
    public interface IProductRepository : IGenericRepository<Product, int>
    {
        IQueryable<Product> SearchProduct(ProductSearchRequest request);

        IEnumerable<Product> GetAll();
    }
}
