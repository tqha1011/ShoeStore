using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class UserVoucherService(
    IUserVoucherRepository userVoucherRepository,
    IUserRepository userRepository,
    IVoucherRepository voucherRepository,
    IUnitOfWork unitOfWork)
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
                VoucherId = v.VoucherId,
                VoucherGuid = v.Voucher!.PublicId,
                VoucherName = v.Voucher.VoucherName,
                Description = v.Voucher.VoucherDescription ?? string.Empty,
                Discount = v.Voucher.Discount,
                ValidFrom = v.Voucher.ValidFrom,
                ValidTo = v.Voucher.ValidTo,
                IsUsed = v.IsUsed,
                SavedAt = v.SavedAt,
                DiscountType = v.Voucher.DiscountType,
                VoucherScope = v.Voucher.VoucherScope,
                MinOrderPrice = v.Voucher.MinOrderPrice
            })
            .ToListAsync(token);

        var result = new PageResult<ResponseVoucherUserDto>
        {
            Items = vouchers,
            TotalCount = totalCount,
            PageSize = pageSize,
            PageNumber = pageIndex
        };
        return result;
    }

    public async Task<ErrorOr<Created>> ClaimUserVoucherAsync(Guid userId, Guid voucherId, CancellationToken token)
    {
        var validUser = await userRepository.GetUserByPublicIdAsync(userId, token);
        if (validUser == null) return Error.NotFound("User.NotFound", "User does not exist");

        // Get the voucher with valid quantity and valid time
        var validVoucher = await voucherRepository.CheckVoucherValidateAsync(voucherId, token);
        if (validVoucher == null) return Error.Validation("Voucher.NotValid", "Voucher does not exist or is not valid");

        var voucherExists = validUser.UserVouchers.Any(uv => uv.VoucherId == validVoucher.Id);
        if (voucherExists) return Error.Conflict("Voucher.AlreadyClaimed", "You have already claimed this voucher.");

        var newVoucherUser = new UserVoucher
        {
            UserId = validUser.Id,
            VoucherId = validVoucher.Id,
            IsUsed = false,
            SavedAt = DateTime.UtcNow
        };
        userVoucherRepository.Add(newVoucherUser);
        validVoucher.TotalQuantity -= 1;
        try
        {
            await unitOfWork.SaveChangesAsync(token);
            return Result.Created;
        }
        catch (DbUpdateConcurrencyException)
        {
            return Error.Conflict("Voucher.ClaimFailed",
                "Failed to claim voucher due to concurrency issues. Please try again.");
        }
    }
}