namespace ShoeStore.Application.DTOs.VoucherDtos
{
    public class UpdateVoucherDto
    { 
        public string? VoucherDescription { get; set; }
        public int? VoucherScope { get; set; }
        public int? DiscountType { get; set; }
        public decimal MaxPriceDiscount { get; set; }
        public DateTime UpdateAt { get; set; } = DateTime.UtcNow;
        public DateTime? ValidFrom { get; set; }
        public DateTime? ValidTo { get; set; }
        public int? MaxUsagePerUser { get; set; }
        public int? TotalQuantity { get; set; }
        public bool? IsDeleted { get; set; } = false;
        public decimal? MinOrderPrice { get; set; }
    }
}
