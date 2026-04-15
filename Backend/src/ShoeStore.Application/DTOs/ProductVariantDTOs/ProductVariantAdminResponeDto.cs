namespace ShoeStore.Application.DTOs.ProductVariantDTOs
{
    public class ProductVariantAdminResponeDto
    {
        public decimal Price { get; set; } = 0;
        public string StockStatus { get; set; } = string.Empty;
        public int Stock { get; set; } = 0;
        public string imgUrl { get; set; } = string.Empty;
    }
}
