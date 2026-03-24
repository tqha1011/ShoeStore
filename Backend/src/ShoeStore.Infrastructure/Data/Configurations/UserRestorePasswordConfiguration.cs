using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class UserRestorePasswordConfiguration : IEntityTypeConfiguration<UserRestorePassword>
{
    public void Configure(EntityTypeBuilder<UserRestorePassword> builder)
    {
        builder.Property(x => x.PublicId)
            .HasDefaultValueSql("gen_random_uuid()");

        builder.HasIndex(c => c.PublicId).IsUnique();

        builder.Property(x => x.Token)
            .IsRequired();
        
        builder.Property(x => x.Expiration)
            .IsRequired();
        
        builder.HasIndex(x => x.UserId);
        
        builder.HasOne(x => x.User)
            .WithMany()
            .HasForeignKey(x => x.UserId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}