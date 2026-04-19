using ShoeStore.Application.DTOs.VoucherDtos;
using ErrorOr;
namespace ShoeStore.Application.Interface.VoucherInterface
{
    public interface IVoucherService
    {
            Task<ErrorOr<Created>> CreateVoucherAsync(CreateVoucherDto voucherCreateDto, CancellationToken token);
            //Task<ErrorOr<VoucherDto>> GetVoucherByGuidAsync(Guid voucherGuid, CancellationToken token);
            //Task<ErrorOr<IEnumerable<VoucherDto>>> GetAllVouchersAsync(CancellationToken token);
            Task<ErrorOr<Updated>> UpdateVoucherAsync(Guid voucherGuid, UpdateVoucherDto voucherUpdateDto, CancellationToken token);
            //Task<ErrorOr<Deleted>> DeleteVoucherAsync(Guid voucherGuid, CancellationToken token );
    }
}
