using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories
{
    public class UserVoucherRepository(AppDbContext context) : IUserVoucherRepository
    {
        public IQueryable<UserVoucher> GetAllVouchers()
        {
            return context.UserVouchers;
        }

        public IQueryable<UserVoucher> GetVouchersByUserGuid(Guid userGuid)
        {
            return context.UserVouchers
                .Where(uv => uv.User != null && uv.User.PublicId == userGuid);
        }
    }
}
