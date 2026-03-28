
using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class ProductResponseDto
    {
        public Guid PublicId { get; set; }
        public string ProductName { get; set; } = string.Empty;
        public string Brand { get; set; } = string.Empty;
        public List<ProductVariantResponeDto> Variants { get; set; } = new List<ProductVariantResponeDto>();
    }
}
