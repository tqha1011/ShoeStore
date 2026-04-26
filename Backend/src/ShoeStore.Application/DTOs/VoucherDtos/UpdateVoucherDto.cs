using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.VoucherDtos
{
    public class UpdateVoucherDto
    {
        public string? VoucherDescription { get; set; }
        public decimal? Discount { get; set; }
        public int? VoucherScope { get; set; }
        public int? DiscountType { get; set; }
        public decimal? MaxPriceDiscount { get; set; }
        private DateTime? _validFrom;
        public DateTime? ValidFrom
        {
            get => _validFrom;
            set => _validFrom = value.HasValue ? DateTime.SpecifyKind(value.Value, DateTimeKind.Utc) : null;
        }

        private DateTime? _validTo;
        public DateTime? ValidTo
        {
            get => _validTo;
            set => _validTo = value.HasValue ? DateTime.SpecifyKind(value.Value, DateTimeKind.Utc) : null;
        }
        public int? MaxUsagePerUser { get; set; }
        public int? TotalQuantity { get; set; }
        public decimal? MinOrderPrice { get; set; }
    }
}
