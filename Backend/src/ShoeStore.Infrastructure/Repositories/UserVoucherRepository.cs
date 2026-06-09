using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class UserVoucherRepository(AppDbContext context)
    : GenericRepository<UserVoucher, int>(context), IUserVoucherRepository
{
    public IQueryable<UserVoucher> GetAllVouchers()
    {
        return DbSet;
    }

    public IQueryable<UserVoucher> GetVouchersByUserGuid(Guid userGuid)
    {
        return DbSet.Where(uv => uv.User!.PublicId == userGuid);
    }

    public void AddListUserVoucher(List<UserVoucher> userVouchers)
    {
        DbSet.AddRange(userVouchers);
    }

    public async Task<List<UserVoucher>> GetUserVouchersByIds(List<int> userVoucherIds, int userId,
        CancellationToken token)
    {
        if (userVoucherIds.Count == 0)
            return [];
        return await DbSet.Where(uv => userVoucherIds.Contains(uv.VoucherId) && uv.UserId == userId)
            .Include(uv => uv.Voucher).ToListAsync(token);
    }
}