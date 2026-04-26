using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.VoucherInterface
{
    public interface IVoucherRepository : IGenericRepository<Voucher, int>
    {
        IQueryable<Voucher> GetAllVouchers();
        IQueryable<Voucher> GetVoucherByGuid(Guid voucherGuid);
    }
}
