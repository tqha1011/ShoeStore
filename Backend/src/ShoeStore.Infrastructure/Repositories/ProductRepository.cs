using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;
using ShoeStore.Application.Extensions;

namespace ShoeStore.Infrastructure.Repositories
{
    public class ProductRepository : IProductRepository, IGenericRepository<Product, int>
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

        public IQueryable<Product> SeachProduct(string? keyWord, string? brand, int? color, int? size, int? productId,
            decimal? minPrice, decimal? maxPric, string? sort)
        {
            return _context.Products.ApplySearch(keyWord).ApplyBrand(brand).ApplyColorId(color).ApplySizeId(size).
                ApplyProductId(productId).ApplyPriceRange(minPrice, maxPric).ApplySort(sort);
        }
    }
}
