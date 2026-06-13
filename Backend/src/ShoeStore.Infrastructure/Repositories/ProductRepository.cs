using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class ProductRepository(AppDbContext context) : GenericRepository<Product, int>(context), IProductRepository
{
    public async Task<Product?> GetDetailsByGuidAsync(Guid productGuid, CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .AsSplitQuery()
            .Include(x => x.ProductVariants)
            .ThenInclude(x => x.Size)
            .Include(x => x.ProductVariants)
            .ThenInclude(x => x.Color)
            .Include(x => x.Category)
            .FirstOrDefaultAsync(x => x.PublicId == productGuid, token);
    }

    public async Task<Product?> GetProductInformationByPublicIdAsync(Guid productGuid, CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .AsSplitQuery()
            .Where(p => !p.IsDeleted && p.PublicId == productGuid &&
                        p.ProductVariants.Any(v => v.IsSelling && !v.IsDeleted))
            .Include(x => x.ProductVariants.Where(v => v.IsSelling && !v.IsDeleted))
            .ThenInclude(x => x.Size)
            .Include(x => x.ProductVariants.Where(v => v.IsSelling && !v.IsDeleted))
            .ThenInclude(x => x.Color)
            .Include(x => x.Category)
            .FirstOrDefaultAsync(token);
    }

    public IQueryable<Product> SearchProduct(ProductSearchRequest request)
    {
        return DbSet.Where(p => !p.IsDeleted)
            .ApplySearch(request.Keyword)
            .ApplyBrand(request.Brand)
            .ApplyColorId(request.ListColorId)
            .ApplySizeId(request.ListSizeId)
            .ApplyProductId(request.ProductId)
            .ApplyCategoryId(request.CategoryId)
            .ApplyPriceRange(request.MinPrice, request.MaxPrice)
            .ApplySort(request.Sort)
            .AsQueryable();
    }

    public async Task<Product?> GetForUpdateByGuidAsync(Guid productGuid, CancellationToken token)
    {
        return await DbSet.Where(p => !p.IsDeleted)
            .Include(x => x.ProductVariants)
            .Include(x => x.Category)
            .FirstOrDefaultAsync(x => x.PublicId == productGuid, token);
    }

    public IQueryable<Product> GetAll()
    {
        return DbSet.Where(p => !p.IsDeleted);
    }

    public IQueryable<Product> GetProductsInformation()
    {
        return DbSet.AsNoTracking()
            .AsSplitQuery()
            .Where(p => !p.IsDeleted && !p.ProductEmbeddings.Any() &&
                        p.ProductVariants.Any(v => v.IsSelling && !v.IsDeleted))
            .Include(x => x.ProductVariants.Where(v => v.IsSelling && !v.IsDeleted))
            .ThenInclude(x => x.Size)
            .Include(x => x.ProductVariants.Where(v => v.IsSelling && !v.IsDeleted))
            .ThenInclude(x => x.Color)
            .Include(x => x.Category);
    }

    public async Task<List<Product>> GetProductsForRagInventoryAsync(IReadOnlyCollection<int> productIds,
        CancellationToken token)
    {
        if (productIds.Count == 0) return [];

        return await DbSet.AsNoTracking()
            .AsSplitQuery()
            .Where(p => !p.IsDeleted && productIds.Contains(p.Id))
            .Include(x => x.ProductVariants.Where(v => v.IsSelling && !v.IsDeleted))
            .ThenInclude(x => x.Size)
            .Include(x => x.ProductVariants.Where(v => v.IsSelling && !v.IsDeleted))
            .ThenInclude(x => x.Color)
            .Include(x => x.Category)
            .ToListAsync(token);
    }

    public async Task<int> CountActiveProductAsync(CancellationToken token)
    {
        return await DbSet.Where(p => !p.IsDeleted && p.ProductVariants.Any(v => v.IsSelling && !v.IsDeleted))
            .CountAsync(token);
    }

    public async Task<ProductResultDto?> CheckProductVariantExistsAsync(Guid productId, int colorId, int sizeId,
        CancellationToken token)
    {
        return await DbSet.Where(p => !p.IsDeleted && p.PublicId == productId)
            .Select(p => new ProductResultDto(
                p.ProductVariants.Any(v => v.IsSelling && !v.IsDeleted && v.ColorId == colorId && v.SizeId == sizeId)))
            .FirstOrDefaultAsync(token);
    }
}
