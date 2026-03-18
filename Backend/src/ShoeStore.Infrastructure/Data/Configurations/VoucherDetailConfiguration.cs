using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class VoucherDetailConfiguration : IEntityTypeConfiguration<VoucherDetail>
{
    public void Configure(EntityTypeBuilder<VoucherDetail> builder)
    {
        builder.HasKey(c => c.Id);
        
        builder.HasOne(c => c.Invoice)
            .WithMany(c => c.VoucherDetails)
            .HasForeignKey(c => c.InvoiceId)
            .OnDelete(DeleteBehavior.Restrict);
        
        builder.HasOne(c => c.Voucher)
            .WithMany()
            .HasForeignKey(c => c.VoucherId)
            .OnDelete(DeleteBehavior.Restrict);
        
        builder.HasIndex(c => new { c.VoucherId, c.InvoiceId }).IsUnique();
    }
}