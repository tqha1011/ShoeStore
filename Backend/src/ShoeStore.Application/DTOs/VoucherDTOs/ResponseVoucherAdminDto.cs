namespace ShoeStore.Application.DTOs.VoucherDTOs;

public class ResponseVoucherAdminDto
{
    private DateTime? _validFrom;

    private DateTime? _validTo;
    public Guid VoucherGuid { get; set; }
    public string? VoucherName { get; set; }
    public decimal Discount { get; set; } = 0;
    public int? VoucherScope { get; set; }
    public int? DiscountType { get; set; }
    public decimal? MaxPriceDiscount { get; set; }
    public decimal? MinOrderPrice { get; set; }

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
}