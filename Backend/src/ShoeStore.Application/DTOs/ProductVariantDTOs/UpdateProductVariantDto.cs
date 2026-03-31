namespace ShoeStore.Application.DTOs.ProductVariantDTOs;

// Frontend send this DTO to update product variant.
// If PublicId is null, it will create new product variant, otherwise it will update existing product variant
public class UpdateProductVariantDto
{
    public Guid? PublicId { get; set; }
    public int SizeId { get; set; }
    public int? ColorId { get; set; }
    public int Stock { get; set; }
    public decimal Price { get; set; }
    public string? ImageUrl { get; set; }
    public bool IsSelling { get; set; }
}