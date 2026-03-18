using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class UserConfiguration : IEntityTypeConfiguration<User>
{
    public void Configure(EntityTypeBuilder<User> builder)
    {
        builder.HasKey(p => p.Id);
        builder.Property(p => p.UserName)
            .HasMaxLength(255);
        builder.Property(p => p.Email)
            .HasMaxLength(255)
            .IsRequired();
        builder.Property(p => p.Password)
            .HasMaxLength(255)
            .IsRequired();
        builder.Property(p => p.Address)
            .HasMaxLength(255);
        builder.Property(p => p.DateOfBirth)
            .HasColumnType("date");
        builder.HasIndex(p => p.Email)
            .IsUnique();
        builder.HasMany(c => c.CartItems).WithOne(c => c.User)
            .HasForeignKey(c => c.UserId);
        builder.HasMany(c => c.Invoices).WithOne(c => c.User)
            .HasForeignKey(c => c.UserId);
    }
}