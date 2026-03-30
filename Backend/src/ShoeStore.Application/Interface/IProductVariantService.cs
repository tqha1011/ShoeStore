using ErrorOr;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface
{
    public interface IProductVariantService
    {
        Task<ErrorOr<ProductVariantResponeDto>> CreateAsync(Guid productGuid, CreateProductVariantDto dto, CancellationToken token);
    }
}
