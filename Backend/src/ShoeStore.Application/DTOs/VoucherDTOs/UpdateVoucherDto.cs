using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.VoucherDTOs;

public class UpdateVoucherDto
{
    public string? VoucherDescription { get; set; }
    public decimal? Discount { get; set; }
    public VoucherScope? VoucherScope { get; set; }
    public DiscountType? DiscountType { get; set; }
    public decimal? MaxPriceDiscount { get; set; }

    public DateTime? ValidFrom { get; set; }

    public DateTime? ValidTo { get; set; }

    public int? MaxUsagePerUser { get; set; }
    public int? TotalQuantity { get; set; }
    public decimal? MinOrderPrice { get; set; }
}