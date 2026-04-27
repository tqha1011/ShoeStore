namespace ShoeStore.Application.DTOs.VoucherDTOs;

public class ResponseVoucherUserDto
{
    public Guid VoucherGuid { get; set; }
    public string VoucherName { get; set; } = string.Empty;
    public string Description { get; set; } = string.Empty;
    public decimal Discount { get; set; } = 0;
    public DateTime? ValidFrom { get; set; } = DateTime.UtcNow;

    public DateTime? ValidTo { get; set; }
}