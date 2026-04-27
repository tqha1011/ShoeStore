using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.VoucherInterface;

namespace ShoeStore.Application.Services;

public class UserVoucherService(IUserVoucherRepository userVoucherRepository, IUserRepository userRepository)
    : IUserVoucherService
{
    public async Task<ErrorOr<PageResult<ResponseVoucherUserDto>>> GetAllVoucherForUserAsync(Guid userGuid,
        CancellationToken token)
    {
        var validUser = await userRepository.CheckUserExistsAsync(userGuid, token);
        if (!validUser)
        {
            return Error.NotFound("User.NotFound","User does not exist");
        }
        
        var vouchers = await userVoucherRepository
            .GetVouchersByUserGuid(userGuid)
            .Where(v => !v.Voucher!.IsDeleted && v.Voucher.ValidTo > DateTime.UtcNow)
            .Select(v => new ResponseVoucherUserDto
            {
                VoucherGuid = v.Voucher!.PublicId,
                VoucherName = v.Voucher.VoucherName,
                Description = v.Voucher.VoucherDescription ?? string.Empty,
                Discount = v.Voucher.Discount,
                ValidFrom = v.Voucher.ValidFrom,
                ValidTo = v.Voucher.ValidTo
            })
            .ToListAsync(token);

        var result = new PageResult<ResponseVoucherUserDto>
        {
            Items = vouchers,
            TotalCount = vouchers.Count
        };
        return result;
    }
}