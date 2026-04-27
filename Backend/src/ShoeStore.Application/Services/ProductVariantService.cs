using ErrorOr;
using Microsoft.Extensions.Caching.Hybrid;
using ShoeStore.Application.Constants;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class ProductVariantService(
    IUnitOfWork uow,
    IProductVariantRepository productVariantRepository,
    IProductRepository productRepository,
    HybridCache cache)
    : IProductVariantService
{
    public async Task<ErrorOr<Created>> CreateAsync(Guid productGuid, CreateProductVariantDto dto, CancellationToken token)
    {
        var product = await productRepository.GetForUpdateByGuidAsync(productGuid, token);
        if (product == null) return Error.NotFound("Product.NotFound", "Product not found.");
        var productVariant = new ProductVariant
        {
            ProductId = product.Id,
            SizeId = dto.SizeId ?? 1,
            ColorId = dto.ColorId ?? 1,
            Stock = dto.Stock ?? 0,
            Price = dto.Price,
            ImageUrl = dto.ImageUrl,
            IsSelling = dto.IsSelling,
            IsDeleted = false
        };

        productVariantRepository.Add(productVariant);
        await uow.SaveChangesAsync(token);
        await cache.RemoveAsync(CacheKey.GenerateProductDetailsCacheKey(productGuid), token);
        await cache.RemoveByTagAsync(CacheTag.Product, token);

        return Result.Created;
    }

    public async Task<ErrorOr<Updated>> UpdateAsync(Guid productVariantGuid, UpdateProductVariantDto dto, CancellationToken token)
    {
        var variant = await productVariantRepository.GetByGuidAsync(productVariantGuid, token);

        if (variant == null)
            return Error.NotFound("ProductVariant.NotFound", "Product variant not found.");

        variant.SizeId = dto.SizeId ?? variant.SizeId;
        variant.ColorId = dto.ColorId ?? variant.ColorId;
        variant.Stock = dto.Stock ?? variant.Stock;
        variant.Price = dto.Price ?? variant.Price;
        variant.ImageUrl = dto.ImageUrl ?? variant.ImageUrl;
        variant.IsSelling = dto.IsSelling ?? variant.IsSelling;

        productVariantRepository.Update(variant);
        await uow.SaveChangesAsync(token);

        var product = await productRepository.GetByIdAsync(variant.ProductId, token);
        if (product != null)
        {
            await cache.RemoveAsync(CacheKey.GenerateProductDetailsCacheKey(product.PublicId), token);
        }
        await cache.RemoveByTagAsync(CacheTag.Product, token);

        return Result.Updated;
    }

}