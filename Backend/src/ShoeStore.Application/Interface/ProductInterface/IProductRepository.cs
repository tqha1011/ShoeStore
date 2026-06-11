using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.ProductInterface;

public interface IProductRepository : IGenericRepository<Product, int>
{
    IQueryable<Product> SearchProduct(ProductSearchRequest request);
    Task<Product?> GetForUpdateByGuidAsync(Guid productGuid, CancellationToken token);

    Task<Product?> GetDetailsByGuidAsync(Guid productGuid, CancellationToken token);

    Task<Product?> GetProductInformationByPublicIdAsync(Guid productGuid, CancellationToken token);

    IQueryable<Product> GetAll();

    IQueryable<Product> GetProductsInformation();

    Task<int> CountActiveProductAsync(CancellationToken token);

    Task<ProductResultDto?> CheckProductVariantExistsAsync(Guid productId, int colorId, int sizeId,
        CancellationToken token);
}
