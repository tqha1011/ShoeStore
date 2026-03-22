using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class InvoiceDetailConfiguration : IEntityTypeConfiguration<InvoiceDetail>
{
    public void Configure(EntityTypeBuilder<InvoiceDetail> builder)
    {
        builder.HasKey(c => c.Id);
        builder.Property(c => c.UnitPrice)
            .IsRequired()
            .HasColumnType("numeric(18,2)");

        builder.Property(c => c.Quantity)
            .IsRequired()
            .HasDefaultValue(1);
        
        builder.HasIndex(c => new { c.ProductVariantId, c.InvoiceId }).IsUnique();

        builder.HasOne(c => c.Invoice)
            .WithMany(c => c.InvoiceDetails)
            .HasForeignKey(c => c.InvoiceId)
            .OnDelete(DeleteBehavior.Restrict);
        
        builder.HasOne(c => c.ProductVariant)
            .WithMany()
            .HasForeignKey(c => c.ProductVariantId)
            .OnDelete(DeleteBehavior.Restrict);
        
        builder.HasIndex(c => c.PublicId).IsUnique();
        
        builder.Property(c => c.PublicId)
            .HasDefaultValueSql("gen_random_uuid()");
    }
}