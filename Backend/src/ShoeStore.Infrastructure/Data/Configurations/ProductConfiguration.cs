using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class ProductConfiguration : IEntityTypeConfiguration<Product>
{
    public void Configure(EntityTypeBuilder<Product> builder)
    {
        builder.HasKey(p => p.Id);
        builder.Property(p => p.ProductName)
            .HasMaxLength(255)
            .IsRequired();
        builder.Property(p => p.Brand)
            .HasMaxLength(255);

        builder.HasIndex(p => p.PublicId).IsUnique();

        builder.Property(p => p.PublicId)
            .HasDefaultValueSql("gen_random_uuid()");

        builder.HasOne(p => p.Category)
            .WithMany(c => c.Products)
            .HasForeignKey(p => p.CategoryId)
            .OnDelete(DeleteBehavior.Restrict);
    }
}