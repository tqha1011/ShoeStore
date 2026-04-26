using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.DTOs.ProductDTOs;

public class UpdateProductDto : ProductBaseDto
{
    public List<UpdateProductVariantDto> ProductVariants { get; set; } = new();
}