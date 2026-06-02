namespace ShoeStore.Application.DTOs.ProductVariantDTOs;

public class CreateProductVariantDto
{
    public int? SizeId { get; set; }
    public int? ColorId { get; set; }
    public int? Stock { get; set; }
    public decimal Price { get; set; }
    public string? ImageUrl { get; set; }
    public bool IsSelling { get; set; }
}