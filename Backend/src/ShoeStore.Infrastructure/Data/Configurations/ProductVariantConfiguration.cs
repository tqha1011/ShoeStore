using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class ProductVariantConfiguration : IEntityTypeConfiguration<ProductVariant>
{
    public void Configure(EntityTypeBuilder<ProductVariant> builder)
    {
        builder.HasKey(c => c.Id);
        
        builder.Property(c => c.ImageUrl)
            .HasMaxLength(255);
        
        builder.Property(c => c.Price)
            .HasColumnType("numeric(18,2)")
            .IsRequired();
        
        builder.Property(c => c.Stock)
            .IsRequired()
            .HasDefaultValue(0);
        
        builder.Property(c => c.IsDeleted)
            .HasDefaultValue(false);
        
        builder.Property(c => c.IsSelling)
            .HasDefaultValue(true);

        builder.HasQueryFilter(c => !c.IsDeleted && c.IsSelling);
        
        builder.HasOne(c => c.Product)
            .WithMany(p => p.ProductVariants)
            .HasForeignKey(c => c.ProductId)
            .OnDelete(DeleteBehavior.Restrict);
        
        builder.HasOne(c => c.Color)
            .WithMany()
            .HasForeignKey(c => c.ColorId)
            .OnDelete(DeleteBehavior.Restrict);
        
        builder.HasOne(c => c.Size)
            .WithMany()
            .HasForeignKey(c => c.SizeId)
            .OnDelete(DeleteBehavior.Restrict);
        
        builder.HasIndex(c => new { c.ProductId, c.ColorId, c.SizeId }).IsUnique();
        
        builder.HasIndex(c => new { c.IsDeleted, c.IsSelling, c.Price });
        
        builder.Property(c => c.PublicId)
            .HasDefaultValueSql("gen_random_uuid()");
        
        builder.HasIndex(c => c.PublicId).IsUnique();
    }
}