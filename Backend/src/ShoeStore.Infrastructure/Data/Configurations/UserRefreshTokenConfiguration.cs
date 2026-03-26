using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class UserRefreshTokenConfiguration : IEntityTypeConfiguration<UserRefreshToken>
{
    public void Configure(EntityTypeBuilder<UserRefreshToken> builder)
    {
        builder.HasIndex(c => c.PublicId).IsUnique();
        builder.Property(c => c.PublicId)
            .HasDefaultValueSql("gen_random_uuid()");
        
        builder.Property(c => c.Token).IsRequired();
        
        builder.Property(c => c.IsRevoked)
            .HasDefaultValue(false);
        
        builder.Property(c => c.Expired)
            .IsRequired();
        
        builder.HasOne(c => c.User)
            .WithMany()
            .HasForeignKey(c => c.UserId)
            .OnDelete(DeleteBehavior.Cascade);
        
        builder.HasIndex(c => c.UserId);
    }
}