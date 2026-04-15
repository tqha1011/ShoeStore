namespace ShoeStore.Application.DTOs.ProductVariantDTOs;

public class ProductVariantResponseDto
{
    public Guid PublicId { get; set; }
    public int SizeId { get; set; }
    public decimal? Size { get; set; }
    public int? ColorId { get; set; }
    public string? ColorName { get; set; }
    public int Stock { get; set; }
    public decimal Price { get; set; }
    public string? ImageUrl { get; set; }
    public bool IsSelling { get; set; }
    public bool IsDelete { get; set; }
}