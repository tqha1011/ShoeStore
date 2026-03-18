using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class UserVoucherConfiguration : IEntityTypeConfiguration<UserVoucher>
{
    public void Configure(EntityTypeBuilder<UserVoucher> builder)
    {
        builder.HasKey(c => c.Id);
        
        builder.Property(c => c.SavedAt)
            .HasColumnType("timestamp with time zone");
        
        builder.Property(c => c.UsedAt)
            .HasColumnType("timestamp with time zone");
        
        builder.Property(c => c.IsUsed)
            .IsRequired()
            .HasDefaultValue(false);
        
        builder.HasOne(c => c.User)
            .WithMany(u => u.UserVouchers)
            .HasForeignKey(c => c.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasOne(c => c.Voucher)
            .WithMany()
            .HasForeignKey(c => c.VoucherId)
            .OnDelete(DeleteBehavior.Restrict);
        
        builder.HasIndex(c => new { c.UserId, c.VoucherId }).IsUnique();
    }
}