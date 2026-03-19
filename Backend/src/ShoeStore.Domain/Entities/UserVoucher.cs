using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class UserVoucher : Entity<int>
{
    public required int UserId { get; set; }
    public required User User { get; set; }
    public required int VoucherId { get; set; }
    public required Voucher Voucher { get; set; }
    public bool IsUsed { get; set; } = false;
    public DateTime SavedAt { get; set; } = DateTime.UtcNow;
    public DateTime? UsedAt { get; set; }
}