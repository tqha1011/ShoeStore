using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class VoucherRepository(AppDbContext context) : GenericRepository<Voucher, int>(context), IVoucherRepository
{
    public IQueryable<Voucher> GetAllVouchers(bool isTracking)
    {
        if (isTracking) return DbSet;
        return DbSet.AsNoTracking();
    }

    public IQueryable<Voucher> GetVoucherByGuid(Guid voucherGuid)
    {
        return DbSet.Where(v => v.PublicId == voucherGuid);
    }

    public async Task<Voucher?> CheckVoucherValidateAsync(Guid voucherGuid, CancellationToken token)
    {
        return await DbSet.Where(v =>
                v.PublicId == voucherGuid && v.ValidTo < DateTime.UtcNow && v.TotalQuantity > 0)
            .FirstOrDefaultAsync(token);
    }

    public IQueryable<Voucher> GetValidVouchers()
    {
        return DbSet.AsNoTracking();
    }
}