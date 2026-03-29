using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;
using ErrorOr;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Application.Interface;
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
                    PublicId = p.PublicId,
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
        public async Task<ErrorOr<ProductResponseDto>> GetProductByGuidAsync(Guid productGuid, CancellationToken token)
        {
            var product = await _productRepository.GetByGuidAsync(productGuid, token);

            if(product == null)
                return Error.NotFound("Product.NotFound", $"Product with ID '{productGuid}' was not found.");

            // Map Product entity to ProductResponseDto
            var productDto = new ProductResponseDto
            {
                PublicId = product.PublicId,
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
        public async Task<ErrorOr<Guid>> AddProductAsync(CreateProductDto dto, CancellationToken token)
        {
            // Create new Product entity
            var product = new Product
            {
                ProductName = dto.ProductName,
                Brand = dto.Brand ?? string.Empty,
                ProductVariants = new List<ProductVariant>()
            };

            foreach (var v in dto.Variants)
            {
                var newVariant = new ProductVariant
                {
                    SizeId = v.SizeId,
                    ColorId = v.ColorId,
                    Stock = v.Stock,
                    Price = v.Price,
                    ImageUrl = v.ImageUrl,
                    IsSelling = v.IsSelling,
                    Product = product,
                    ProductId = product.Id,
                    IsDeleted = false
                };

                product.ProductVariants.Add(newVariant);
            }

            // Add product to repository and save
            _productRepository.Add(product);
            await _uow.SaveChangesAsync(token);

            return product.PublicId;
        }
        public async Task<ErrorOr<Updated>> UpdateProductAsync(Guid productGuid, UpdateProductDto dto, CancellationToken token)
        {
            var product = await _productRepository.GetByGuidAsync(productGuid,token);

            if (product == null)
                return Error.NotFound("Product.NotFound", $"Product with ID '{productGuid}' was not found.");

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
                    existingVariant.Product = product;
                }
            }
            var dtoPublicIds = dto.Variants
                .Select(v => v.PublicId)
                .ToHashSet();

            var variantsToRemove = existingVariants
                .Where(v => !dtoPublicIds.Contains(v.PublicId))
                .ToList();

            foreach (var variant in variantsToRemove)
                product.ProductVariants.Remove(variant);

            // Update product in repository and save
            _productRepository.Update(product);
            await _uow.SaveChangesAsync(token);

            return Result.Updated;
            
        }
        public async Task<ErrorOr<Deleted>> DeleteProductAsync(Guid productGuid, CancellationToken token)
        {
           

            // Fetch product from repository
            var product = await _productRepository.GetByGuidAsync(productGuid, token);

            // Check if product exists
            if (product == null)
                return Error.NotFound(code: "Product.NotFound", description: $"Product with ID {productGuid} not found.");

            // Soft delete: mark all variants as deleted
            foreach (var variant in product.ProductVariants)
            {
                variant.IsDeleted = true;
            }
            await _uow.SaveChangesAsync(token);

            return Result.Deleted;
           
        }
    }
}
