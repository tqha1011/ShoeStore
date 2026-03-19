using System.Globalization;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

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

        builder.HasData(
            new User
            {
                Id = 1,
                UserName = "admin1",
                Email = "admin1@gmail.com",
                Password = "$2a$20$OTMgqqRjT5H.eoJtIAWqvuGWjiVyq8L36wAYDUUS55hbiLDNkvV1K",
                Role = UserRole.Admin,
                CreatedAt = DateTime.SpecifyKind(new DateTime(2026,3,19), DateTimeKind.Utc)
            });
    }
}