using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class UserAddress : Entity<int>
{
    public string Address { get; set; } = string.Empty;
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public required int UserId { get; set; }
    public User? User { get; set; }

    public bool IsDefault { get; set; } = false;
}