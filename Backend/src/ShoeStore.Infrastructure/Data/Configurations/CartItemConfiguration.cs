using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class CartItemConfiguration : IEntityTypeConfiguration<CartItem>
{
    public void Configure(EntityTypeBuilder<CartItem> builder)
    {
        builder.HasKey(c => c.Id);
        builder.Property(c => c.Quantity)
            .IsRequired()
            .HasDefaultValue(1);

        // composite index for UserId and ProductVariantId to ensure a user cannot have duplicate product variants in their cart
        builder.HasIndex(c => new { c.UserId, c.ProductVariantId }).IsUnique();

        builder.HasOne(c => c.User)
            .WithMany(u => u.CartItems)
            .HasForeignKey(c => c.UserId)
            .OnDelete(DeleteBehavior.Cascade);

        builder.HasOne(c => c.ProductVariant)
            .WithMany()
            .HasForeignKey(c => c.ProductVariantId)
            .OnDelete(DeleteBehavior.Cascade);
        
        builder.Property(c => c.PublicId)
            .HasDefaultValueSql("gen_random_uuid()");

        builder.HasIndex(c => c.PublicId).IsUnique();
    }
}