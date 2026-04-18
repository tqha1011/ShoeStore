
namespace ShoeStore.Application.DTOs.VoucherDtos
{
    public class CreateVoucherDto
    {
        public string? VocherName { get; set; }
        public string? VoucherDescription { get; set; }
        public decimal Discount { get; set; }
        public int VoucherScope { get; set; }
        public int DiscountType { get; set; }
        public decimal MaxPriceDiscount { get; set; }
        public DateTime? ValidFrom { get; set; }
        public DateTime? ValidTo { get; set; }
        public int? MaxUsagePerUser { get; set; }
        public int TotalQuantity { get; set; }
        public decimal MinOrderPrice { get; set; }
    }
}
