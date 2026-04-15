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
            .Include(x => x.ProductVariants)
            .ThenInclude(x => x.Size)
            .Include(x => x.ProductVariants)
            .ThenInclude(x => x.Color)
            .FirstOrDefaultAsync(x => x.PublicId == productGuid, token);
    }

    public IQueryable<Product> SearchProduct(ProductSearchRequest request)
    {
        return DbSet.ApplySearch(request.Keyword)
            .ApplyBrand(request.Brand)
            .ApplyColorId(request.ListColorId)
            .ApplySizeId(request.ListSizeId)
            .ApplyProductId(request.ProductId)
            .ApplyPriceRange(request.MinPrice, request.MaxPrice)
            .ApplySort(request.Sort)
            .AsQueryable();
    }

    public async Task<Product?> GetForUpdateByGuidAsync(Guid productGuid, CancellationToken token)
    {
        return await DbSet.Include(x => x.ProductVariants)
            .FirstOrDefaultAsync(x => x.PublicId == productGuid, token);
    }

    public IQueryable<Product> GetAll()
    {
        return DbSet;
    }
}