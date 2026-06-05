using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;
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
                v.PublicId == voucherGuid && v.ValidTo >= DateTime.UtcNow && v.TotalQuantity > 0 &&
                v.ValidFrom <= DateTime.UtcNow
                && !v.IsDeleted && v.ReleaseType == ReleaseType.ManualAssign)
            .FirstOrDefaultAsync(token);
    }

    public IQueryable<Voucher> GetValidVouchers()
    {
        return DbSet.AsNoTracking();
    }

    public async Task<Dictionary<int, Voucher>> GetVouchersByIdsAsync(List<int> voucherGuids,
        CancellationToken token)
    {
        if (voucherGuids.Count == 0) return new Dictionary<int, Voucher>();
        return await DbSet.AsNoTracking().Where(v => voucherGuids.Contains(v.Id) && !v.IsDeleted)
            .ToDictionaryAsync(v => v.Id, token);
    }
}