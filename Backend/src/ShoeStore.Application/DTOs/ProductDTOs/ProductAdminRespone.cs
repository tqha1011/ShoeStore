using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.DTOs.ProductDTOs;

public class ProductAdminResponse
{
    public Guid PublicId { get; set; }
    public string ProductName { get; set; } = string.Empty;
    public List<ProductVariantAdminResponseDto> Variants { get; set; } = new();
}