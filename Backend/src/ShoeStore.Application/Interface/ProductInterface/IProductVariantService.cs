using ErrorOr;
using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.Interface.ProductInterface;

public interface IProductVariantService
{
    Task<ErrorOr<Created>> CreateAsync(Guid productGuid, CreateProductVariantDto dto, CancellationToken token);
    Task<ErrorOr<Updated>> UpdateAsync(Guid productVariantGuid, UpdateProductVariantDto dto, CancellationToken token);
}