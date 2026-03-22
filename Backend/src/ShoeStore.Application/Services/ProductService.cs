using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services
{
    public class ProductService : IProductService
    {
        private readonly IUnitOfWork _uow;
        private readonly IProductRepository _productRepository;

        public ProductService(IUnitOfWork uow, IProductRepository productRepository)
        {
            _uow = uow;
            _productRepository = productRepository;
        }

        public async Task<IEnumerable<Product>> GetProductAsync(string? keyWord, string? brand, int? color, int? size, int? productId,
            decimal? minPrice, decimal? maxPrice, string? sort, int pageIndex, int pageSize)
        {
            return await _productRepository
        .SeachProduct(keyWord, brand, color, size, productId, minPrice, maxPrice, sort, pageIndex, pageSize)
        .ToListAsync();
        }
    }
}
