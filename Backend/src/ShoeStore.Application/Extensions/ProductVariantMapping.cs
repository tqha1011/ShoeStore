using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Extensions;

public static class ProductVariantMapping
{
    public static ProductVariantResponseDto MapProductVariantResponseDto(this ProductVariant productVariant)
    {
        return new ProductVariantResponseDto
        {
            PublicId = productVariant.PublicId,
            SizeId = productVariant.SizeId,
            Size = productVariant.Size?.Size ?? 0,
            ColorId = productVariant.ColorId,
            ColorName = productVariant.Color?.ColorName ?? string.Empty,
            Stock = productVariant.Stock,
            IsSelling = productVariant.IsSelling,
            ImageUrl = productVariant.ImageUrl ?? string.Empty,
            Price = productVariant.Price,
            IsDelete = productVariant.IsDeleted
        };
    }
}