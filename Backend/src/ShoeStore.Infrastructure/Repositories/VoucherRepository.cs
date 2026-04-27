using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories
{
    public class VoucherRepository(AppDbContext context) : GenericRepository<Voucher, int>(context), IVoucherRepository
    {
        public IQueryable<Voucher> GetAllVouchers()
        {
            return context.Vouchers.AsNoTracking();
        }

        public IQueryable<Voucher> GetVoucherByGuid(Guid voucherGuid)
        {
            return context.Vouchers.Where(v => v.PublicId == voucherGuid).AsNoTracking();
        }
    }
}
