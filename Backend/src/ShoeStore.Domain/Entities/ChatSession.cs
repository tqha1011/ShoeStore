using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class ChatSession : Entity<int>
{
    public Guid PublicId { get; set; } = Guid.NewGuid();

    public string? Title { get; set; }

    public required int UserId { get; set; }

    public User? User { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public bool IsActive { get; set; } = true;
}