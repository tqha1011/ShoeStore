using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Application.DTOs.ProducVariantDTOs;

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

        public async Task AddProduct(CreateProductDTO dto)
        {
            var product = new Product
            {
                PublicId = new Guid(),
                ProductName = dto.ProductName,
                Brand = dto.Brand
            };
            _productRepository.Add(product);
            await _uow.SaveChangesAsync();
        }
        public async Task UpdateProduct(int id, UpdateProductDTO dto, CancellationToken token)
        {
            var product = await _productRepository.GetByIdAsync(id, token);
            if (product == null)
                throw new Exception("Could not find the product");

            product.ProductName = dto.ProductName;
            product.Brand = dto.Brand;

            _productRepository.Update(product);
            await _uow.SaveChangesAsync();
        }

        public async Task<IEnumerable<ProductResponseDTO>> GetProductAsync(ProductSearchRequest request)
        {
            var query =  _productRepository.SeachProduct(request);
            var itiems =  query.Select(p => new ProductResponseDTO
            {
                Id = p.Id,
                ProductName = p.ProductName,
                Brand = p.Brand,

                // Lấy giá thấp nhất trong các biến thể của sản phẩm
                MinPrice = p.ProductVariants.Any() ? p.ProductVariants.Min(v => v.Price) : 0,

                // Lấy danh sách tên màu (distinct để không bị lặp)
                AvailableColors = p.ProductVariants
                        .Select(v => v.Color.ColorName)
                        .Distinct()
                        .ToList(),

                // Lấy danh sách size số
                AvailableSizes = p.ProductVariants
                        .Select(v => v.Size.Size)
                        .Distinct()
                        .ToList(),

                // Lấy cái ảnh đầu tiên làm Thumbnail
                ThumbnailUrl = p.ProductVariants
                        .Select(v => v.ImageUrl)
                        .FirstOrDefault()
            });

            return await itiems.ToListAsync();
        }
    }
}
