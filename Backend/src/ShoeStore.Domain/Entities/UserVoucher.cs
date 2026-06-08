using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class UserVoucher : Entity<int>
{
    public Guid PublicId { get; set; } = Guid.NewGuid();
    public required int UserId { get; set; }
    public User? User { get; set; }
    public required int VoucherId { get; set; }
    public Voucher? Voucher { get; set; }
    public bool IsUsed { get; set; } = false;
    public DateTime SavedAt { get; set; } = DateTime.UtcNow;

    public int UsedCount { get; set; } = 0;

    public int ReservedCount { get; set; } = 0;
    public DateTime? UsedAt { get; set; }

    public DateTime? ReservedAt { get; set; }
}