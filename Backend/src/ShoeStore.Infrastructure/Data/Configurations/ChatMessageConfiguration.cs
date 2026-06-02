using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class ChatMessageConfiguration : IEntityTypeConfiguration<ChatMessage>
{
    public void Configure(EntityTypeBuilder<ChatMessage> builder)
    {
        builder.HasKey(c => c.Id);

        builder.HasOne(c => c.Session).WithMany().HasForeignKey(c => c.SessionId);

        builder.HasIndex(c => new { c.SessionId, c.CreatedAt });

        builder.HasIndex(c => c.PublicId).IsUnique();

        builder.Property(c => c.Content).IsRequired();

        // turn enum to string 
        builder.Property(c => c.Role).HasConversion<string>();
    }
}