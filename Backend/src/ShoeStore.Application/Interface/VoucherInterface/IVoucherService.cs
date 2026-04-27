using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.VoucherDTOs;

namespace ShoeStore.Application.Interface.VoucherInterface;

public interface IVoucherService
{
    // CREATE
    Task<ErrorOr<Created>> CreateVoucherAsync(CreateVoucherDto voucherCreateDto, CancellationToken token);

    // GET
    Task<ErrorOr<PageResult<ResponseVoucherAdminDto>>> GetVoucherForAdminAsync(CancellationToken token,
        int pageIndex = 1, int pageSize = 10);

    // UPDATE
    Task<ErrorOr<Updated>> UpdateVoucherAsync(Guid voucherGuid, UpdateVoucherDto voucherUpdateDto,
        CancellationToken token);

    // DELETE
    Task<ErrorOr<Deleted>> DeleteVoucherByGuidAsync(Guid voucherGuid, CancellationToken token);

    Task<ErrorOr<Deleted>> DeleteVoucherExpireAsync(CancellationToken token);

    // NOTIFY
    Task<ErrorOr<Success>> NotifyUserAboutNewVoucherAsync(string adminEmail, string voucherName, DateTime validTo,
        CancellationToken token);
}