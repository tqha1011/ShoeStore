using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.VoucherDTOs;

public class ResponseVoucherUserDto
{
    public int VoucherId { get; set; }
    public Guid VoucherGuid { get; set; }
    public string VoucherName { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public decimal Discount { get; set; } = 0;
    public DateTime? ValidFrom { get; set; } = DateTime.UtcNow;

    public DateTime? ValidTo { get; set; }

    public DiscountType DiscountType { get; set; }

    public VoucherScope VoucherScope { get; set; }

    public decimal MinOrderPrice { get; set; }

    public bool IsUsed { get; set; }

    public DateTime SavedAt { get; set; }
}