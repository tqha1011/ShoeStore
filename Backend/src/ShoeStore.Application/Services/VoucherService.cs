using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.Notification;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services;

public class VoucherService(
    INotificationQueue queue,
    IUnitOfWork uow,
    IUserRepository userRepository,
    IVoucherRepository voucherRepository) : IVoucherService
{
    public async Task<ErrorOr<Created>> CreateVoucherAsync(CreateVoucherDto voucherCreateDto, CancellationToken token)
    {
        var voucher = new Voucher
        {
            VoucherName = voucherCreateDto.VoucherName ?? string.Empty,
            VoucherDescription = voucherCreateDto.VoucherDescription ?? string.Empty,
            Discount = voucherCreateDto.Discount ?? 0,
            VoucherScope = voucherCreateDto.VoucherScope,
            DiscountType = voucherCreateDto.DiscountType,
            MaxPriceDiscount = voucherCreateDto.MaxPriceDiscount,
            ValidFrom = voucherCreateDto.ValidFrom ?? DateTime.UtcNow,
            ValidTo = voucherCreateDto.ValidTo,
            MaxUsagePerUser = voucherCreateDto.MaxUsagePerUser,
            TotalQuantity = voucherCreateDto.TotalQuantity ?? 0,
            MinOrderPrice = voucherCreateDto.MinOrderPrice ?? 0,
            IsDeleted = false
        };

        voucherRepository.Add(voucher);
        await uow.SaveChangesAsync(token);
        await NotifyUserAboutNewVoucherAsync(voucher.Id, voucher.VoucherName, voucher.ValidTo ?? DateTime.UtcNow,
            token);
        return Result.Created;
    }

    public async Task<ErrorOr<Deleted>> DeleteVoucherByGuidAsync(Guid voucherGuid, CancellationToken token)
    {
        var voucher = await voucherRepository
            .GetVoucherByGuid(voucherGuid)
            .FirstOrDefaultAsync(token);
        if (voucher == null)
            return Error.NotFound(
                "VOUCHER_NOT_FOUND",
                "The voucher with the specified GUID does not exist."
            );
        if (voucher.IsDeleted) return Result.Deleted;

        // Soft delete logic
        voucher.IsDeleted = true;
        voucher.UpdatedAt = DateTime.UtcNow;
        await uow.SaveChangesAsync(token);
        return Result.Deleted;
    }

    public async Task<ErrorOr<Deleted>> DeleteVoucherExpireAsync(CancellationToken token)
    {
        var deletedCounts = await voucherRepository
            .GetAllVouchers(true)
            .Where(v => v.ValidTo < DateTime.UtcNow && !v.IsDeleted)
            .ExecuteUpdateAsync(s => s
                .SetProperty(v => v.IsDeleted, true)
                .SetProperty(v => v.UpdatedAt, DateTime.UtcNow), token);
        return Result.Deleted;
    }

    public async Task<ErrorOr<Success>> NotifyUserAboutNewVoucherAsync(int voucherId, string voucherName,
        DateTime validTo, CancellationToken token)
    {
        var users = await userRepository
            .GetAllUsers()
            .Where(u => u.Role == UserRole.User)
            .Select(u => new VoucherTargetUserDto(u.Id, u.Email, u.UserName))
            .ToListAsync(token);

        var newVoucherNotification = new VoucherNotificationDto(users, voucherId, voucherName, validTo);
        await queue.EnqueueAsync(newVoucherNotification, token);

        return Result.Success;
    }

    public async Task<ErrorOr<Updated>> UpdateVoucherAsync(Guid voucherGuid, UpdateVoucherDto voucherUpdateDto,
        CancellationToken token)
    {
        var voucher = await voucherRepository
            .GetVoucherByGuid(voucherGuid)
            .FirstOrDefaultAsync(token);

        if (voucher == null)
            return Error.NotFound(
                "VOUCHER_NOT_FOUND",
                "The voucher with the specified GUID does not exist."
            );

        voucher.VoucherDescription = voucherUpdateDto.VoucherDescription ?? voucher.VoucherDescription;

        voucher.Discount = voucherUpdateDto.Discount ?? voucher.Discount;

        voucher.VoucherScope = voucherUpdateDto.VoucherScope ?? voucher.VoucherScope;
        voucher.DiscountType = voucherUpdateDto.DiscountType ?? voucher.DiscountType;

        voucher.MaxPriceDiscount = voucherUpdateDto.MaxPriceDiscount ?? voucher.MaxPriceDiscount;

        voucher.ValidFrom = voucherUpdateDto.ValidFrom ?? voucher.ValidFrom;
        voucher.ValidTo = voucherUpdateDto.ValidTo ?? voucher.ValidTo;

        voucher.MaxUsagePerUser = voucherUpdateDto.MaxUsagePerUser ?? voucher.MaxUsagePerUser;
        voucher.TotalQuantity = voucherUpdateDto.TotalQuantity ?? voucher.TotalQuantity;
        voucher.MinOrderPrice = voucherUpdateDto.MinOrderPrice ?? voucher.MinOrderPrice;

        voucher.UpdatedAt = DateTime.UtcNow;


        await uow.SaveChangesAsync(token);
        return Result.Updated;
    }

    public async Task<ErrorOr<PageResult<ResponseVoucherAdminDto>>> GetVoucherForAdminAsync(CancellationToken token,
        int pageIndex = 1, int pageSize = 10)
    {
        var query = voucherRepository.GetAllVouchers(false);
        var filteredVouchers = query.Where(v => !v.IsDeleted);
        var totalCount = await filteredVouchers.CountAsync(token);

        var vouchers = await filteredVouchers
            .OrderByDescending(x => x.CreatedAt)
            .ApplyPagination(pageIndex, pageSize)
            .Select(v => new ResponseVoucherAdminDto
            {
                VoucherGuid = v.PublicId,
                VoucherName = v.VoucherName,
                Discount = v.Discount,
                VoucherScope = v.VoucherScope,
                DiscountType = v.DiscountType,
                MaxPriceDiscount = v.MaxPriceDiscount,
                ValidFrom = v.ValidFrom,
                ValidTo = v.ValidTo,
                MinOrderPrice = v.MinOrderPrice,
                Quantity = v.TotalQuantity
            })
            .ToListAsync(token);

        var pageResult = new PageResult<ResponseVoucherAdminDto>
        {
            Items = vouchers,
            TotalCount = totalCount,
            PageSize = pageSize,
            PageNumber = pageIndex
        };
        return pageResult;
    }
}