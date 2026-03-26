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
        public async Task<ErrorOr<PageResult<ProductResponseDto>>> GetProductsAsync(ProductSearchRequest request, CancellationToken token)
        {
            // Validate pagination parameters
            if (request.PageIndex <= 0)
                return Error.Validation("Pagination.PageIndex", "Page index must be greater than 0.");

            if (request.PageSize <= 0 || request.PageSize > 50)
                return Error.Validation("Pagination.PageSize", "Page size must be between 1 and 50.");

            try
            {
                // Get the queryable result from repository
                var query = _productRepository.SearchProduct(request);

                // Get total count before applying paging
                var totalCount = await query.CountAsync(token);

                // Apply pagination and fetch products
                var products = await query
                    .Select(p => new ProductResponseDto
                    {
                        Id = p.Id,
                        ProductName = p.ProductName,
                        Brand = p.Brand ?? string.Empty,
                        AvailableColors = p.ProductVariants
                            .Where(v => v.IsSelling && !v.IsDeleted && v.Color != null)
                            .Select(v => v.Color!.ColorName)
                            .Distinct()
                            .ToList(),
                        AvailableSizes = p.ProductVariants
                            .Where(v => v.IsSelling && !v.IsDeleted && v.Size != null)
                            .Select(v => v.Size!.Size)
                            .Distinct()
                            .ToList(),
                        MinPrice = p.ProductVariants
                            .Where(v => v.IsSelling && !v.IsDeleted)
                            .Where(v => v.IsSelling && !v.IsDeleted)
                            .Min(v => (decimal?)v.Price) ?? 0,
                        ThumbnailUrl = p.ProductVariants
                            .Where(v => v.IsSelling && !v.IsDeleted)
                            .Select(v => v.ImageUrl!)
                            .ToList(),
                    })
                    .ToListAsync(token);

                // Build and return paged result
                var pageResult = new PageResult<ProductResponseDto>
                {
                    Items = products,
                    TotalCount = totalCount,
                    PageNumber = request.PageIndex,
                    PageSize = request.PageSize
                };

                return pageResult;
            }
            catch (Exception ex)
            {
                return Error.Unexpected(
                   code: "Product.SearchFailed",
                   description: ex.Message + " | " + ex.InnerException?.Message
                );
            }
        }
        public async Task<ErrorOr<ProductResponseDto>> GetProductByIdAsync(int id, CancellationToken token)
        {
            // Validate input
            if (id <= 0)
                return Error.Validation("Product.Id", "Product ID must be greater than 0.");

            // Fetch product from repository
            var product = await _productRepository.GetByIdAsync(id, token);

            // Check if product exists
            if (product == null)
                return Error.NotFound(code: "Product.NotFound", description: $"Product with ID {id} not found.");

            // Map Product entity to ProductResponseDto
            var productDto = new ProductResponseDto
            {
                Id = product.Id,
                ProductName = product.ProductName,
                Brand = product.Brand ?? string.Empty,
                AvailableColors = product.ProductVariants
                    .Where(v => v.IsSelling && !v.IsDeleted && v.Color != null)
                    .Select(v => v.Color!.ColorName)
                    .Distinct()
                    .ToList(),
                AvailableSizes = product.ProductVariants
                    .Where(v => v.IsSelling && !v.IsDeleted && v.Size != null)
                    .Select(v => v.Size!.Size)
                    .Distinct()
                    .ToList(),
                MinPrice = product.ProductVariants
                    .Where(v => v.IsSelling && !v.IsDeleted)
                    .DefaultIfEmpty()
                    .Min(v => v?.Price ?? 0),
                ThumbnailUrl = product.ProductVariants
                            .Where(v => v.IsSelling && !v.IsDeleted)
                            .Select(v => v.ImageUrl!)
                            .Distinct()
                            .ToList()
            };

            return productDto;
        }
        public async Task<ErrorOr<Created>> AddProductAsync(CreateProductDto dto, CancellationToken token)
        {
            // Validate product name
            if (string.IsNullOrWhiteSpace(dto.ProductName))
                return Error.Validation("Product.ProductName", "Product name is required.");

            try
            {
                // Create new Product entity
                var product = new Product
                {
                    ProductName = dto.ProductName,
                    Brand = dto.Brand ?? string.Empty,
                    ProductVariants = new List<ProductVariant>()
                };

                // Add product to repository and save
                _productRepository.Add(product);
                await _uow.SaveChangesAsync(token);

                return Result.Created;
            }
            catch (Exception ex)
            {
                return Error.Unexpected(code: "Product.CreationFailed",
                    description: "An error occurred while creating the product.");
            }
        }
        public async Task<ErrorOr<Updated>> UpdateProductAsync(int id, UpdateProductDto dto, CancellationToken token)
        {
            // Validate product ID
            if (id <= 0)
                return Error.Validation("Product.Id", "Product ID must be greater than 0.");

            // Validate product name
            if (string.IsNullOrWhiteSpace(dto.ProductName))
                return Error.Validation("Product.ProductName", "Product name is required.");

            // Fetch product from repository
            var product = await _productRepository.GetByIdAsync(id, token);

            // Check if product exists
            if (product == null)
                return Error.NotFound(code: "Product.NotFound", description: $"Product with ID {id} not found.");

            try
            {
                // Update basic product information
                product.ProductName = dto.ProductName;
                product.Brand = dto.Brand ?? string.Empty;

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
                        existingVariant.IsDeleted = true;
                    }
                }

                // Update product in repository and save
                _productRepository.Update(product);
                await _uow.SaveChangesAsync(token);

                return Result.Updated;
            }
            catch (Exception ex)
            {
                return Error.Unexpected(code: "Product.UpdateFailed",
                    description: "An error occurred while updating the product.");
            }
        }
        public async Task<ErrorOr<Deleted>> DeleteProductAsync(int id, CancellationToken token)
        {
            // Validate product ID
            if (id <= 0)
                return Error.Validation("Product.Id", "Product ID must be greater than 0.");

            // Fetch product from repository
            var product = await _productRepository.GetByIdAsync(id, token);

            // Check if product exists
            if (product == null)
                return Error.NotFound(code: "Product.NotFound", description: $"Product with ID {id} not found.");

            try
            {
                // Soft delete: mark all variants as deleted
                foreach (var variant in product.ProductVariants)
                {
                    variant.IsDeleted = true;
                }

                // Delete product from repository and save
                _productRepository.Delete(product);
                await _uow.SaveChangesAsync(token);

                return Result.Deleted;
            }
            catch (Exception ex)
            {
                return Error.Unexpected(code: "Product.DeletionFailed",
                    description: "An error occurred while deleting the product.");
            }
        }
    }
}
