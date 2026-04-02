using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class UserRestorePassword : Entity<int>
{
    public Guid PublicId { get; set; } = Guid.NewGuid();
    public required string Token { get; set; }
    public DateTime Expiration { get; set; }
    public int UserId { get; set; }
    public User User { get; set; } = null!;
    public bool IsUsed { get; set; } = false;
}