using ErrorOr;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Caching.Hybrid;
using Microsoft.Extensions.Logging;
using ShoeStore.Application.Constants;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class ProductService(
    IUnitOfWork uow,
    IProductRepository productRepository,
    HybridCache cache,
    ILogger<ProductService> logger) : IProductService
{
    public async Task<ErrorOr<PageResult<ProductResponseDto>>> GetProductsUserAsync(ProductSearchRequest request,
        CancellationToken token)
    {
        // Validate pagination parameters
        if (request.PageIndex <= 0)
            return Error.Validation("Pagination.PageIndex", "Page index must be greater than 0.");

        if (request.PageSize is <= 0 or > 50)
            return Error.Validation("Pagination.PageSize", "Page size must be between 1 and 50.");


        // Get the queryable result from repository
        var query = productRepository.SearchProduct(request);

        // Apply pagination and fetch products
        var cachedListProduct = await cache.GetOrCreateAsync(
            CacheKey.GenerateProductListCacheKey(request),
            async cancel =>
            {
                // Get total count before applying paging
                var totalCount = await query.Where(p => p.ProductVariants.Any(v => v.IsSelling && !v.IsDeleted))
                    .CountAsync(cancel);

                var products = await query
                    .ApplyPaging(request.PageIndex, request.PageSize)
                    .Select(p => new ProductResponseDto
                    {
                        PublicId = p.PublicId,
                        Brand = p.Brand ?? string.Empty,
                        ProductName = p.ProductName,
                        CategoryId = p.CategoryId,
                        CategoryName = p.Category != null ? p.Category.Name : string.Empty,
                        Variants = p.ProductVariants
                            .Where(v => v.IsSelling && !v.IsDeleted)
                            .Select(v => new ProductVariantResponseDto
                            {
                                PublicId = v.PublicId,
                                SizeId = v.SizeId,
                                Size = v.Size != null ? v.Size.Size : 0,
                                ColorId = v.ColorId,
                                ColorName = v.Color != null ? v.Color.ColorName : null,
                                Stock = v.Stock,
                                Price = v.Price,
                                ImageUrl = v.ImageUrl,
                                IsSelling = v.IsSelling
                            })
                            .ToList()
                    })
                    .ToListAsync(cancel);

                var pageResult = new PageResult<ProductResponseDto>
                {
                    Items = products,
                    TotalCount = totalCount,
                    PageNumber = request.PageIndex,
                    PageSize = request.PageSize
                };
                return pageResult;
            },
            tags: [CacheTag.Product],
            cancellationToken: token);

        return cachedListProduct;
    }

    public async Task<ErrorOr<ProductResponseDto>> GetProductByGuidAsync(Guid productGuid, CancellationToken token)
    {
        // Try to get product details from cache. If not found, fetch from database and cache it for future requests.
        var cachedProductDto = await cache.GetOrCreateAsync(
            CacheKey.GenerateProductDetailsCacheKey(productGuid),
            async cancel =>
            {
                var productEntity = await productRepository.GetDetailsByGuidAsync(productGuid, cancel);
                if (productEntity == null) return null;

                var productDto = new ProductResponseDto
                {
                    PublicId = productEntity.PublicId,
                    ProductName = productEntity.ProductName,
                    Brand = productEntity.Brand ?? string.Empty,
                    CategoryName = productEntity.Category?.Name ?? string.Empty,
                    CategoryId = productEntity.CategoryId,
                    Variants = productEntity.ProductVariants
                        .Select(v => new ProductVariantResponseDto
                        {
                            PublicId = v.PublicId,
                            SizeId = v.SizeId,
                            Size = v.Size?.Size ?? 0,
                            ColorId = v.ColorId,
                            ColorName = v.Color?.ColorName,
                            Stock = v.Stock,
                            Price = v.Price,
                            ImageUrl = v.ImageUrl,
                            IsSelling = v.IsSelling
                        })
                        .ToList()
                };
                return productDto;
            },
            cancellationToken: token);

        // if in cache, return it directly without accessing the database
        return cachedProductDto != null
            ? cachedProductDto
            : Error.NotFound("Product.NotFound", $"Product with ID {productGuid} was not found.");
    }

    public async Task<ErrorOr<Guid>> AddProductAsync(CreateProductDto dto, CancellationToken token)
    {
        // Create new Product entity
        var product = new Product
        {
            ProductName = dto.ProductName,
            Brand = dto.Brand ?? string.Empty,
            CategoryId = dto.CategoryId,
            ProductVariants = new List<ProductVariant>()
        };

        foreach (var v in dto.Variants)
        {
            var newVariant = new ProductVariant
            {
                SizeId = v.SizeId,
                ColorId = v.ColorId,
                Stock = v.Stock,
                Price = v.Price,
                ImageUrl = v.ImageUrl,
                IsSelling = v.IsSelling,
                Product = product,
                ProductId = product.Id,
                IsDeleted = false
            };

            product.ProductVariants.Add(newVariant);
        }

        // Add product to repository and save
        productRepository.Add(product);
        await uow.SaveChangesAsync(token);
        try
        {
            await cache.RemoveByTagAsync(CacheTag.Product, token);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Error while adding product to cache");
        }

        return product.PublicId;
    }

    public async Task<ErrorOr<Updated>> UpdateProductAsync(Guid productGuid, UpdateProductDto dto,
        CancellationToken token)
    {
        var product = await productRepository.GetForUpdateByGuidAsync(productGuid, token);

        if (product == null)
            return Error.NotFound("Product.NotFound", $"Product with ID '{productGuid}' was not found.");

        // Update basic product information
        product.ProductName = dto.ProductName;
        product.Brand = dto.Brand ?? string.Empty;
        product.CategoryId = dto.CategoryId;

        // Get existing variants for comparison
        var existingVariants = product.ProductVariants.ToList();

        // Process variants: update existing, add new, delete removed
        foreach (var variantDto in dto.Variants)
        {
            var existingVariant = existingVariants.FirstOrDefault(v => v.PublicId == variantDto.PublicId);

            if (existingVariant != null)
            {
                // Update existing variant
                existingVariant.SizeId = variantDto.SizeId;
                existingVariant.ColorId = variantDto.ColorId;
                existingVariant.Stock = variantDto.Stock;
                existingVariant.IsSelling = variantDto.IsSelling;
                existingVariant.ImageUrl = variantDto.ImageUrl;
                existingVariant.Price = variantDto.Price;
                existingVariant.UpdatedAt = DateTime.UtcNow;
            }
            else
            {
                // if existing variant is null , it means a new product variant is added, so we will add it to the product
                var newProductVariant = new ProductVariant
                {
                    SizeId = variantDto.SizeId,
                    ColorId = variantDto.ColorId,
                    Stock = variantDto.Stock,
                    Price = variantDto.Price,
                    ImageUrl = variantDto.ImageUrl,
                    IsSelling = variantDto.IsSelling,
                    ProductId = product.Id
                };
                product.ProductVariants.Add(newProductVariant);
            }
        }

        var dtoPublicIds = dto.Variants
            .Select(v => v.PublicId)
            .ToHashSet();

        var variantsToRemove = existingVariants
            .Where(v => !dtoPublicIds.Contains(v.PublicId))
            .ToList();

        // soft delete
        foreach (var variant in variantsToRemove) variant.IsDeleted = true;

        // Update product in repository and save
        productRepository.Update(product);
        await uow.SaveChangesAsync(token);
        try
        {
            await cache.RemoveAsync(CacheKey.GenerateProductDetailsCacheKey(product.PublicId), token);
            await cache.RemoveByTagAsync(CacheTag.Product, token);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Error while updating product.");
        }

        return Result.Updated;
    }

    public async Task<ErrorOr<Deleted>> DeleteProductAsync(Guid productGuid, CancellationToken token)
    {
        // Fetch product from repository
        var product = await productRepository.GetForUpdateByGuidAsync(productGuid, token);

        // Check if product exists
        if (product == null)
            return Error.NotFound("Product.NotFound", $"Product with ID {productGuid} not found.");

        // Soft delete: mark all variants as deleted
        foreach (var variant in product.ProductVariants) variant.IsDeleted = true;
        product.IsDeleted = true;
        await uow.SaveChangesAsync(token);
        try
        {
            await cache.RemoveAsync(CacheKey.GenerateProductDetailsCacheKey(productGuid), token);
            await cache.RemoveByTagAsync(CacheTag.Product, token);
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Error while deleting product.");
        }

        return Result.Deleted;
    }

    public async Task<ErrorOr<PageResult<ProductAdminRespone>>> GetProductsAdminAsync(ProductAdminRequestDto request,
        CancellationToken token)
    {
        var query = productRepository.GetAll().ApplyStock(request).AsNoTracking().Where(p => !p.IsDeleted);

        var totalCount = await query.CountAsync(token);

        var products = await query.Select(p => new ProductAdminRespone
        {
            PublicID = p.PublicId,
            ProductName = p.ProductName,
            Variants = p.ProductVariants
                .Where(v => v.IsSelling && !v.IsDeleted)
                .Select(v => new ProductVariantAdminResponeDto
                {
                    imgUrl = v.ImageUrl,
                    Price = v.Price,
                    Stock = v.Stock,
                    StockStatus = v.Stock <= 0 ? "Out of Stock" :
                        v.Stock < 10 ? "Low Stock" : "In Stock"
                })
                .ToList()
        }).ToListAsync(token);

        if (products.Count == 0)
            return Error.NotFound("Product not found");
        var pageResult = new PageResult<ProductAdminRespone>
        {
            Items = products,
            TotalCount = totalCount,
            PageNumber = request.PageIndex,
            PageSize = request.PageSize
        };

        return pageResult;
    }
}