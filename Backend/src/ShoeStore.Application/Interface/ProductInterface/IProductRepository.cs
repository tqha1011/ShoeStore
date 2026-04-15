using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.ProductInterface;

public interface IProductRepository : IGenericRepository<Product, int>
{
    IQueryable<Product> SearchProduct(ProductSearchRequest request);
    Task<Product?> GetForUpdateByGuidAsync(Guid productGuid, CancellationToken token);

    Task<Product?> GetDetailsByGuidAsync(Guid productGuid, CancellationToken token);

    IQueryable<Product> GetAll();
}