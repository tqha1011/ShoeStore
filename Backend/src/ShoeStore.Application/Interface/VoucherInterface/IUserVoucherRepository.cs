using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.VoucherInterface
{
    public interface IUserVoucherRepository
    {
        IQueryable<UserVoucher> GetAllVouchers();
        IQueryable<UserVoucher> GetVouchersByUserGuid(Guid userGuid);
    }
}
