using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Extensions;

namespace ShoeStore.Infrastructure.Repositories
{
    public class ProductRepository(AppDbContext context) : GenericRepository<Product, int>(context), IProductRepository
    {
        public async Task<Product?> GetByGuidAsync(Guid productGuid, CancellationToken token)
        {
            return await context.Products.FirstOrDefaultAsync(x => x.PublicId == productGuid, token);
        }

        public IQueryable<Product> SearchProduct(ProductSearchRequest request)
        {
            return context.Products.ApplySearch(request.Keyword)
                                                .ApplyBrand(request.Brand)
                                                .ApplyColorId(request.ListColorId)
                                                .ApplySizeId(request.ListSizeId)
                                                .ApplyProductId(request.ProductId)
                                                .ApplyPriceRange(request.MinPrice, request.MaxPrice)
                                                .ApplySort(request.Sort)
                                                .ApplyPaging(request.PageIndex, request.PageSize)
                                                .AsQueryable();
        }
    }
}
