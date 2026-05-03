using ShoeStore.Domain.Common;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Domain.Entities;

public class ChatMessage : Entity<int>
{
    public Guid PublicId { get; set; } = Guid.NewGuid();

    public required string Content { get; set; }

    public required int SessionId { get; set; }

    public ChatSession? Session { get; set; }

    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public int TokenCount { get; set; }

    public ChatBotRole Role { get; set; }
}