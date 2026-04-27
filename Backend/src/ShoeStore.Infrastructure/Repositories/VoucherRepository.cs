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
}