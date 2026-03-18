using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class VoucherConfiguration : IEntityTypeConfiguration<Voucher>
{
    public void Configure(EntityTypeBuilder<Voucher> builder)
    {
        builder.HasKey(c => c.Id);
        
        builder.Property(c => c.VoucherName)
            .IsRequired()
            .HasMaxLength(100);
        
        builder.Property(c => c.VoucherDescription)
            .HasMaxLength(255)
            .HasDefaultValue("Discount for you");
        
        builder.Property(c => c.CreatedAt)
            .HasColumnType("timestamp with time zone");
        
        builder.Property(c => c.UpdatedAt)
            .HasColumnType("timestamp with time zone");
        
        builder.Property(c => c.IsDeleted)
            .HasDefaultValue(false)
            .IsRequired();
        
        builder.Property(c => c.ValidFrom)
            .HasColumnType("timestamp with time zone");
        
        builder.Property(c => c.ValidTo)
            .HasColumnType("timestamp with time zone");
        
        builder.Property(c => c.Discount)
            .HasColumnType("numeric(5,4)")
            .HasDefaultValue(0)
            .IsRequired();

        builder.Property(c => c.MaxUsagePerUser)
            .HasDefaultValue(1);

        builder.Property(c => c.MinOrderPrice)
            .HasColumnType("numeric(18,2)")
            .HasDefaultValue(0);
        
        // HasQueryFilter automatically filter the condition
        builder.HasQueryFilter(c => !c.IsDeleted);
        
        builder.HasIndex(c => c.IsDeleted);
        
        builder.Property(c => c.TotalQuantity)
            .HasDefaultValue(100);
    }
}