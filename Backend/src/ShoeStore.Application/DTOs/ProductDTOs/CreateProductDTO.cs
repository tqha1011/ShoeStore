using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class CreateProductDto : ProductBaseDto
    {
        public List<CreateProductVariantDto> Variants { get; set; } = new List<CreateProductVariantDto>();
    }
}
