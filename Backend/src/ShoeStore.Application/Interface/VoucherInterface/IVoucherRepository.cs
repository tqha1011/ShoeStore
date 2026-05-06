using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.VoucherInterface;

public interface IVoucherRepository : IGenericRepository<Voucher, int>
{
    IQueryable<Voucher> GetAllVouchers(bool isTracking);
    IQueryable<Voucher> GetVoucherByGuid(Guid voucherGuid);
}