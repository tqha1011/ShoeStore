using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.VoucherDTOs;

public class ResponseVoucherAdminDto
{
    public Guid VoucherGuid { get; set; }
    public string? VoucherName { get; set; }
    public decimal Discount { get; set; } = 0;
    public VoucherScope? VoucherScope { get; set; }
    public DiscountType? DiscountType { get; set; }
    public decimal? MaxPriceDiscount { get; set; }
    public decimal? MinOrderPrice { get; set; }

    public DateTime? ValidFrom { get; set; }

    public DateTime? ValidTo { get; set; }
}