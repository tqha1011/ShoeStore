using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.VoucherInterface;

namespace ShoeStore.Application.Services;

public class UserVoucherService(IUserVoucherRepository userVoucherRepository, IUserRepository userRepository)
    : IUserVoucherService
{
    public async Task<ErrorOr<PageResult<ResponseVoucherUserDto>>> GetAllVoucherForUserAsync(Guid userGuid,
        CancellationToken token, int pageIndex = 1,
        int pageSize = 10)
    {
        var validUser = await userRepository.CheckUserExistsAsync(userGuid, token);
        if (!validUser) return Error.NotFound("User.NotFound", "User does not exist");

        var query = userVoucherRepository.GetVouchersByUserGuid(userGuid);

        var filteredVouchers = query.Where(v => !v.Voucher!.IsDeleted && v.Voucher.ValidTo > DateTime.UtcNow);

        var totalCount = await filteredVouchers.CountAsync(token);

        var vouchers = await filteredVouchers
            .OrderBy(v => v.SavedAt)
            .ApplyPagination(pageIndex, pageSize)
            .Select(v => new ResponseVoucherUserDto
            {
                VoucherGuid = v.Voucher!.PublicId,
                VoucherName = v.Voucher.VoucherName,
                Description = v.Voucher.VoucherDescription ?? string.Empty,
                Discount = v.Voucher.Discount,
                ValidFrom = v.Voucher.ValidFrom,
                ValidTo = v.Voucher.ValidTo,
                IsUsed = v.IsUsed,
                SavedAt = v.SavedAt
            })
            .ToListAsync(token);

        var result = new PageResult<ResponseVoucherUserDto>
        {
            Items = vouchers,
            TotalCount = totalCount
        };
        return result;
    }
}