using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Interface.ProductInterface;

public interface IProductService
{
    Task<ErrorOr<PageResult<ProductResponseDto>>> GetProductsUserAsync(ProductSearchRequest request, CancellationToken token);
    Task<ErrorOr<ProductResponseDto>> GetProductByGuidAsync(Guid productGuid, CancellationToken token);
    Task<ErrorOr<Guid>> AddProductAsync(CreateProductDto dto, CancellationToken token);
    Task<ErrorOr<Updated>> UpdateProductAsync(Guid productGuid, UpdateProductDto dto, CancellationToken token);
    Task<ErrorOr<Deleted>> DeleteProductAsync(Guid productGuid, CancellationToken token);
    Task<ErrorOr<PageResult<ProductAdminRespone>>> GetProductsAdminAsync(ProductAdminRequestDto request, CancellationToken token);
}