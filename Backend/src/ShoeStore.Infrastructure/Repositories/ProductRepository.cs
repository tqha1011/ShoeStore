using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories
{
    public class ProductRepository : IProductRepository
    {
        private readonly AppDbContext _context;

        public ProductRepository(AppDbContext context)
        {
            _context = context;
        }

        public void Add(Product product)
        {
            _context.Products.Add(product);
        }
        public void Update(Product product)
        {
            _context.Products.Update(product);
        }
        public void Delete(Product product)
        {
            _context.Products.Remove(product);
        }
        public async Task<Product?> GetByIdAsync(int id, CancellationToken token)
        {
            return await _context.Products.AsNoTracking().FirstOrDefaultAsync(x => x.Id!.Equals(id), token);
        }
        public IQueryable<Product> SearchProduct(ProductSearchRequest request)
        {
            return _context.Products.ApplySearch(request.Keyword).ApplyBrand(request.Brand).ApplyColorId(request.ListColorId).ApplySizeId(request.ListSizeId).
                ApplyProductId(request.ProductId).ApplyPriceRange(request.MinPrice, request.MaxPrice).ApplySort(request.Sort).ApplyPaging(request.PageIndex, request.PageSize);
        }
    }
}
