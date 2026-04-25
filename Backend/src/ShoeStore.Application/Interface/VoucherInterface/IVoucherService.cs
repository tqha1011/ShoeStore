using ShoeStore.Application.DTOs.VoucherDtos;
using ErrorOr;
using ShoeStore.Application.DTOs;
namespace ShoeStore.Application.Interface.VoucherInterface
{
    public interface IVoucherService
    {
        // CREATE
        Task<ErrorOr<Created>> CreateVoucherAsync(CreateVoucherDto voucherCreateDto, CancellationToken token);
        // GET
        Task<ErrorOr<PageResult<ResponseVoucherAdminDto>>> GetVoucherForAdminAsync(CancellationToken token);
        Task<ErrorOr<PageResult<ResponseVoucherAdminDto>>> GetAllVouchersAsync(CancellationToken token);
        // UPDATE
        Task<ErrorOr<Updated>> UpdateVoucherAsync(Guid voucherGuid, UpdateVoucherDto voucherUpdateDto, CancellationToken token);
        // DELETE
        Task<ErrorOr<Deleted>> DeleteVoucherByGuidAsync(Guid voucherGuid, CancellationToken token);
        Task<ErrorOr<Deleted>> DeleteVoucherExpireAsync(CancellationToken token);
        // NOTIFY
        Task<ErrorOr<Success>> NotifyUserAboutNewVoucherAsync(string adminEmail, string voucherName, DateTime validTo, CancellationToken token);
    }
}
