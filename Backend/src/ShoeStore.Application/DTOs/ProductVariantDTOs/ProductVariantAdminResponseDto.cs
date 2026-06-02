namespace ShoeStore.Application.DTOs.ProductVariantDTOs;

public class ProductVariantAdminResponseDto
{
    public decimal Price { get; set; } = 0;
    public string StockStatus { get; set; } = string.Empty;
    public int Stock { get; set; } = 0;
    public string ImgUrl { get; set; } = string.Empty;
}