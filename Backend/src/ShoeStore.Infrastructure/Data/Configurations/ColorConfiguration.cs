using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class ColorConfiguration : IEntityTypeConfiguration<Color>
{
    public void Configure(EntityTypeBuilder<Color> builder)
    {
        builder.HasKey(c => c.Id);
        builder.Property(c => c.ColorName)
            .HasMaxLength(50)
            .IsRequired();

        builder.Property(c => c.HexCode)
            .HasMaxLength(8);
    }
}