namespace ShoeStore.Application.DTOs.ProductVariantDTOs;

// Frontend send this DTO to update product variant.
// If PublicId is null, it will create new product variant, otherwise it will update existing product variant
public class UpdateProductVariantDto
{
    // We don't use PublicId here because one DTO entry can map to multiple variants (Size + multiple Colors)
    public int SizeId { get; set; }
    public List<int> ColorIds { get; set; } = new();
    public int Stock { get; set; }
    public decimal Price { get; set; }
    public string? ImageUrl { get; set; }
    public bool IsSelling { get; set; }
}