using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class ChatSessionConfiguration : IEntityTypeConfiguration<ChatSession>
{
    public void Configure(EntityTypeBuilder<ChatSession> builder)
    {
        builder.HasKey(c => c.Id);

        builder.HasIndex(c => new { c.UserId, c.IsActive });

        builder.HasIndex(c => c.PublicId).IsUnique();

        builder.HasOne(c => c.User).WithMany(u => u.ChatSessions).HasForeignKey(c => c.UserId);
    }
}