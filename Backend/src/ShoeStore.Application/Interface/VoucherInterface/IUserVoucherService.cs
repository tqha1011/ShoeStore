using ShoeStore.Application.DTOs.VoucherDtos;
using ShoeStore.Application.DTOs;
using ErrorOr;
namespace ShoeStore.Application.Interface.VoucherInterface
{
    public interface IUserVoucherService : IVoucherService
    {
        Task<ErrorOr<PageResult<ResponseVoucherUserDto>>> GetAllVoucherForUserAsync(Guid UserGuid, CancellationToken token);
    }
}
