using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class UserVoucher : Entity<int>
{
    public Guid PublicId { get; set; }
    public required int UserId { get; set; }
    public User? User { get; set; }
    public required int VoucherId { get; set; }
    public Voucher? Voucher { get; set; }
    public bool IsUsed { get; set; } = false;
    public DateTime SavedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UsedAt { get; set; }
}