using ShoeStore.Application.DTOs.ProductVariantDTOs;

namespace ShoeStore.Application.DTOs.ProductDTOs
{
    public class ProductAdminRespone
    {
        public Guid PublicID { get; set; }
        public string ProductName { get; set; } = string.Empty;
        public List<ProductVariantAdminResponeDto> Variants { get; set; } = new List<ProductVariantAdminResponeDto>();
    }
}
