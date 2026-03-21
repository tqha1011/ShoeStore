using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class Voucher : Entity<int>
{
    public Guid PublicId { get; set; }
    public required string VoucherName { get; set; }
    public string? VoucherDescription { get; set; }
    public required decimal Discount { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public DateTime UpdatedAt { get; set; }
    public DateTime? ValidFrom { get; set; }
    public DateTime? ValidTo { get; set; }
    public int? MaxUsagePerUser { get; set; }
    
    public int TotalQuantity { get; set; }
    public required bool IsDeleted { get; set; } = false;
    public decimal MinOrderPrice { get; set; } = 0;
    
    /// <summary>
    /// Gets the collection of voucher details associated with this voucher
    /// Representing the invoices that have applied this voucher and the details of their usage.
    /// </summary>
    public ICollection<VoucherDetail> VoucherDetails { get; set; } =  new List<VoucherDetail>();
}