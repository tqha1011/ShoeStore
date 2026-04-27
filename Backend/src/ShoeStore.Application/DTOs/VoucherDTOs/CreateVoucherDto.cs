using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.VoucherDTOs;

public class CreateVoucherDto
{
    private DateTime? _validFrom;

    private DateTime? _validTo;
    public string? VoucherName { get; set; }
    public string? VoucherDescription { get; set; }
    public decimal? Discount { get; set; }
    public VoucherScope VoucherScope { get; set; } = VoucherScope.Product;
    public DiscountType DiscountType { get; set; } = DiscountType.Percentage;
    public decimal MaxPriceDiscount { get; set; }

    public DateTime? ValidFrom
    {
        get => _validFrom;
        set => _validFrom = value.HasValue ? DateTime.SpecifyKind(value.Value, DateTimeKind.Utc) : null;
    }

    public DateTime? ValidTo
    {
        get => _validTo;
        set => _validTo = value.HasValue ? DateTime.SpecifyKind(value.Value, DateTimeKind.Utc) : null;
    }

    public int? MaxUsagePerUser { get; set; }
    public int? TotalQuantity { get; set; }
    public decimal? MinOrderPrice { get; set; }
}