using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ErrorOr;
using ShoeStore.Application.DTOs.ProductVariantDTOs;

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


            // Get the queryable result from repository
            var query = _productRepository.SearchProduct(request);

            // Get total count before applying paging
            var totalCount = await query.CountAsync(token);

            // Apply pagination and fetch products
            var products = await query
                .Select(p => new ProductResponseDto
                {
                    Id = p.Id,
                    Brand = p.Brand ?? string.Empty,
                    ProductName = p.ProductName,
                    Variants = p.ProductVariants
                            .Where(v => v.IsSelling && !v.IsDeleted)
                            .Select(v => new ProductVariantResponeDto
                            {
                                SizeId = v.SizeId,
                                Size = v.Size!.Size,
                                ColorId = v.ColorId,
                                ColorName = v.Color != null ? v.Color.ColorName : null,
                                Stock = v.Stock,
                                Price = v.Price,
                                ImageUrl = v.ImageUrl,
                                IsSelling = v.IsSelling
                            })
                            .ToList()
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
                Variants = product.ProductVariants
                            .Where(v => v.IsSelling && !v.IsDeleted)
                            .Select(v => new ProductVariantResponeDto
                            {
                                SizeId = v.SizeId,
                                Size = v.Size!.Size,
                                ColorId = v.ColorId,
                                ColorName = v.Color != null ? v.Color.ColorName : null,
                                Stock = v.Stock,
                                Price = v.Price,
                                ImageUrl = v.ImageUrl,
                                IsSelling = v.IsSelling
                            })
                            .ToList()
            };

            return productDto;
        }
        public async Task<ErrorOr<Created>> AddProductAsync(CreateProductDto dto, CancellationToken token)
        {
            // Validate product name
            if (string.IsNullOrWhiteSpace(dto.ProductName))
                return Error.Validation("Product.ProductName", "Product name is required.");

           
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

            
            // Update basic product information
            product.ProductName = dto.ProductName;
            product.Brand = dto.Brand ?? string.Empty;

            // Get existing variants for comparison
            var existingVariants = product.ProductVariants.ToList();

            // Process variants: update existing, add new, delete removed
            foreach (var variantDto in dto.Variants)
            {
                var existingVariant = existingVariants.FirstOrDefault(v => v.Product.Id == id);

                if (existingVariant != null)
                {
                    // Update existing variant
                    existingVariant.SizeId = variantDto.SizeId;
                    existingVariant.Size = variantDto.Size;
                    existingVariant.ColorId = variantDto.ColorId;
                    existingVariant.Stock = variantDto.Stock;
                    existingVariant.IsSelling = variantDto.IsSelling;
                    existingVariant.ImageUrl = variantDto.ImageUrl;
                    existingVariant.Price = variantDto.Price;
                    existingVariant.Product = product;
                }
            }
            //var dtoPublicIds = dto.Variants
            //    .Select(v => v.PublicId)
            //    .ToHashSet();

            //var variantsToRemove = existingVariants
            //    .Where(v => !dtoPublicIds.Contains(v.PublicId))
            //    .ToList();

            //foreach (var variant in variantsToRemove)
            //    product.ProductVariants.Remove(variant);

            // Update product in repository and save
            _productRepository.Update(product);
            await _uow.SaveChangesAsync(token);

            return Result.Updated;
            
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
    }
}
