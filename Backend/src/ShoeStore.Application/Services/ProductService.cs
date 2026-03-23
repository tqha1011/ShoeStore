using System;
using System.Collections.Generic;
using System.Text;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ErrorOr;

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

        public void AddProduct(CreateProductDto dto)
        {
            var product = new Product
            {
                PublicId = Guid.NewGuid(),
                ProductName = dto.ProductName,
                Brand = dto.Brand
            };
            _productRepository.Add(product);
            _uow.SaveChangesAsync();
        }
        public async Task<ErrorOr<Success>> UpdateProduct(int id, UpdateProductDto dto, CancellationToken token)
        {
            var product = await _productRepository.GetByIdAsync(id, token);
            if (product == null)
            {
                return Error.NotFound(
                    code: "Product.NotFound",
                    description: $"Không tìm thấy sản phẩm với Id: {id}");
            }

            product.ProductName = dto.ProductName;
            product.Brand = dto.Brand;

            try
            {
                _productRepository.Update(product);
                await _uow.SaveChangesAsync(token);
                return Result.Success;
            }
            catch (Exception)
            {
                return Error.Failure(
                    code: "Product.UpdateError",
                    description: "Có lỗi xảy ra trong quá trình lưu cập nhật.");
            }
        }

        public async Task<ErrorOr<IEnumerable<ProductResponseDto>>> GetProductAsync(ProductSearchRequest request, CancellationToken token)
        {
            var query =  _productRepository.SeachProduct(request);
            var items =  query.Select(p => new ProductResponseDto
            {
                Id = p.Id,
                ProductName = p.ProductName,
                Brand = p.Brand ?? string.Empty,

                // Lấy giá thấp nhất trong các biến thể của sản phẩm
                MinPrice = p.ProductVariants.Where(v => v.IsSelling && !v.IsDeleted)
                                            .Any()? p.ProductVariants
                                            .Where(v => v.IsSelling && !v.IsDeleted)
                                            .Min(v => v.Price) : 0,

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

            return await items.ToListAsync(token);
        }
    }
}
