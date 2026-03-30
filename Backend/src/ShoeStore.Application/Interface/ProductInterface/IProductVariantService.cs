using ErrorOr;
using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.Interface.ProductInterface;

public interface IProductVariantService
{
    Task<ErrorOr<ProductVariantResponseDto>> CreateAsync(Guid productGuid, CreateProductVariantDto dto,
        CancellationToken token);
}