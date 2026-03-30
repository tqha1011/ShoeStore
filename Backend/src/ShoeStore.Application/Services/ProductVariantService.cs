using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services
{
    public class ProductVariantService : IProductVariantService
    {
        private readonly IUnitOfWork _uow;
        private readonly IProductVariantRepository _productVariantRepository;
        private readonly IProductRepository _productRepository;

        public ProductVariantService(IUnitOfWork uow, IProductVariantRepository productVariantRepository, IProductRepository productRepository)
        {
            _uow = uow;
            _productVariantRepository = productVariantRepository;
            _productRepository = productRepository;
        }

        public async Task<ErrorOr<ProductVariantResponeDto>> CreateAsync(Guid productGuid, CreateProductVariantDto dto, CancellationToken token)
        {
            var product = await _productRepository.GetByGuidAsync(productGuid, token);
            if (product == null)
            {
                return Error.NotFound("Product.NotFound", "Product not found.");
            }
            var productVariant = new ProductVariant
            {
                ProductId = product.Id,
                Product = product,
                SizeId = dto.SizeId,
                ColorId = dto.ColorId,
                Stock = dto.Stock,
                Price = dto.Price,
                ImageUrl = dto.ImageUrl,
                IsSelling = dto.IsSelling,
                IsDeleted = false
            };

            _productVariantRepository.Add(productVariant);
            await _uow.SaveChangesAsync(token);

            return new ProductVariantResponeDto
            {
                SizeId = productVariant.SizeId,
                Size = productVariant.Size?.Size ?? 0,
                ColorId = productVariant.ColorId,
                ColorName = productVariant.Color?.ColorName,
                Stock = productVariant.Stock,
                Price = productVariant.Price,
                ImageUrl = productVariant.ImageUrl,
                IsSelling = productVariant.IsSelling,
                IsDelete = productVariant.IsDeleted
            };
        }
    }
}
