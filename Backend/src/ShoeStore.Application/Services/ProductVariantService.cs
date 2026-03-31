using ErrorOr;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class ProductVariantService(
    IUnitOfWork uow,
    IProductVariantRepository productVariantRepository,
    IProductRepository productRepository)
    : IProductVariantService
{
    public async Task<ErrorOr<ProductVariantResponseDto>> CreateAsync(Guid productGuid, CreateProductVariantDto dto,
        CancellationToken token)
    {
        var product = await productRepository.GetForUpdateByGuidAsync(productGuid, token);
        if (product == null) return Error.NotFound("Product.NotFound", "Product not found.");
        var productVariant = new ProductVariant
        {
            ProductId = product.Id,
            SizeId = dto.SizeId,
            ColorId = dto.ColorId,
            Stock = dto.Stock,
            Price = dto.Price,
            ImageUrl = dto.ImageUrl,
            IsSelling = dto.IsSelling,
            IsDeleted = false
        };

        productVariantRepository.Add(productVariant);
        await uow.SaveChangesAsync(token);

        return new ProductVariantResponseDto
        {
            PublicId = productVariant.PublicId,
            SizeId = productVariant.SizeId,
            Size = productVariant.Size?.Size ?? 0,
            ColorId = productVariant.ColorId,
            ColorName = productVariant.Color?.ColorName,
            Stock = productVariant.Stock,
            Price = productVariant.Price,
            ImageUrl = productVariant.ImageUrl,
            IsSelling = productVariant.IsSelling,
            IsDelete = productVariant.IsDeleted
        };
    }
}