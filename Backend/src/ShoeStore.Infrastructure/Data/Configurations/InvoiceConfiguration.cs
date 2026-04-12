using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class InvoiceConfiguration : IEntityTypeConfiguration<Invoice>
{
    public void Configure(EntityTypeBuilder<Invoice> builder)
    {
        builder.HasKey(c => c.Id);

        builder.Property(c => c.Phone)
            .IsRequired()
            .HasMaxLength(20);

        builder.Property(c => c.ShippingAddress)
            .IsRequired()
            .HasMaxLength(255);

        builder.Property(c => c.FinalPrice)
            .IsRequired()
            .HasColumnType("numeric(18,2)");

        builder.Property(c => c.CreatedAt).HasColumnType("timestamp with time zone");

        builder.Property(c => c.UpdatedAt).HasColumnType("timestamp with time zone");

        builder.Property(c => c.Status).IsRequired();

        builder.HasOne(c => c.Payment).WithMany().HasForeignKey(c => c.PaymentId)
            .OnDelete(DeleteBehavior.Restrict);

        builder.HasMany(c => c.InvoiceDetails)
            .WithOne(c => c.Invoice)
            .HasForeignKey(c => c.InvoiceId);

        builder.HasMany(c => c.VoucherDetails)
            .WithOne(c => c.Invoice)
            .HasForeignKey(c => c.InvoiceId);

        builder.Property(c => c.PublicId)
            .HasDefaultValueSql("gen_random_uuid()");

        builder.Property(c => c.FullName)
            .IsRequired();

        builder.HasIndex(c => c.PublicId).IsUnique();

        builder.HasIndex(c => c.OrderCode).IsUnique();

        builder.Property(c => c.ShippingFee).HasColumnType("numeric(18,2)");

        builder.HasIndex(c => new { c.Status, c.CreatedAt });
    }
}