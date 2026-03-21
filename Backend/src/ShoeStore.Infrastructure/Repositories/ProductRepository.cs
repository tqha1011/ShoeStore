using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

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
    }
}
