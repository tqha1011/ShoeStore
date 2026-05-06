using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class UserAddressConfiguration : IEntityTypeConfiguration<UserAddress>
{
    public void Configure(EntityTypeBuilder<UserAddress> builder)
    {
        builder.HasKey(addr => addr.Id);

        builder.Property(addr => addr.Address)
            .IsRequired()
            .HasMaxLength(500);

        builder.HasOne(addr => addr.User)
            .WithMany(u => u.UserAddresses)
            .HasForeignKey(addr => addr.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasIndex(addr => new { addr.UserId, addr.Address }).IsUnique();
        builder.HasIndex(addr => addr.UserId)
            .IsUnique()
            .HasFilter("is_default = true");
    }
}