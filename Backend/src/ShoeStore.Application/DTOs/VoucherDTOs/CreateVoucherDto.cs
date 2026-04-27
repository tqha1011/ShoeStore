using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.VoucherDTOs;

public class CreateVoucherDto
{
    public string? VoucherName { get; set; }
    public string? VoucherDescription { get; set; }
    public decimal? Discount { get; set; }
    public VoucherScope VoucherScope { get; set; } = VoucherScope.Product;
    public DiscountType DiscountType { get; set; } = DiscountType.Percentage;
    public decimal MaxPriceDiscount { get; set; }

    public DateTime? ValidFrom { get; set; }

    public DateTime? ValidTo { get; set; }

    public int? MaxUsagePerUser { get; set; }
    public int? TotalQuantity { get; set; }
    public decimal? MinOrderPrice { get; set; }
}