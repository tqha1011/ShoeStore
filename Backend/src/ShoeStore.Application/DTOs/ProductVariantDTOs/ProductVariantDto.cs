namespace ShoeStore.Application.DTOs.ProductVariantDTOs
{
    public class ProductVariantDto
    {
        public Guid? PublicId { get; set; }
        public decimal? Size { get; set; }
        public List<string>? Colors { get; set; }
        public int? Stock { get; set; }
    }
}
