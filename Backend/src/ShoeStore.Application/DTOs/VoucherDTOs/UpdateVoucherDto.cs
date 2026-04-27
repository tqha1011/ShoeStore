using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.VoucherDTOs;

public class UpdateVoucherDto
{
    private DateTime? _validFrom;

    private DateTime? _validTo;
    public string? VoucherDescription { get; set; }
    public decimal? Discount { get; set; }
    public VoucherScope? VoucherScope { get; set; }
    public DiscountType? DiscountType { get; set; }
    public decimal? MaxPriceDiscount { get; set; }

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