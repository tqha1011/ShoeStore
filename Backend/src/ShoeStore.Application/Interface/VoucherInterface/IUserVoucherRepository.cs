using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.VoucherInterface;

public interface IUserVoucherRepository : IGenericRepository<UserVoucher, int>
{
    IQueryable<UserVoucher> GetAllVouchers();
    IQueryable<UserVoucher> GetVouchersByUserGuid(Guid userGuid);
}