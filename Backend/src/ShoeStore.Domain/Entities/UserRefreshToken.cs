using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class UserRefreshToken : Entity<int>
{
    public Guid PublicId { get; set; }
    public required string Token { get; set; }
    public DateTime Expired { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;
    public bool IsRevoked { get; set; } =  false;
    public int UserId { get; set; }
    public User User { get; set; } = null!;
}