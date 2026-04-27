using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.VoucherDTOs;

namespace ShoeStore.Application.Interface.VoucherInterface;

public interface IUserVoucherService
{
    Task<ErrorOr<PageResult<ResponseVoucherUserDto>>> GetAllVoucherForUserAsync(Guid userGuid, CancellationToken token);
}