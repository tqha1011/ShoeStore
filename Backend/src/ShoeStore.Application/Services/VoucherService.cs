using System.ComponentModel.DataAnnotations;
using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.VoucherDtos;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.Notification;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services
{
    public class VoucherService : IVoucherService
    {
        private readonly IVoucherRepository voucherRepository;
        private readonly IUnitOfWork uow;
        private readonly IUserVoucherRepository userVoucherRepository;
        private readonly IEmailService emailService;
        private readonly IUserRepository userRepository;

        public VoucherService(IVoucherRepository voucherRepository, IUnitOfWork uow, IUserVoucherRepository userVoucherRepository, IEmailService emailService, IUserRepository userRepository)
        {
            this.voucherRepository = voucherRepository;
            this.uow = uow;
            this.userVoucherRepository = userVoucherRepository;
            this.emailService = emailService;
            this.userRepository = userRepository;
        }
        public async Task<ErrorOr<Created>> CreateVoucherAsync(CreateVoucherDto voucherCreateDto, CancellationToken token)
        {

            var voucher = new Voucher
            {
                VoucherName = voucherCreateDto.VoucherName ?? string.Empty,
                VoucherDescription = voucherCreateDto.VoucherDescription,
                Discount = voucherCreateDto.Discount ?? 0,
                VoucherScope = (VoucherScope)voucherCreateDto.VoucherScope,
                DiscountType = (DiscountType)voucherCreateDto.DiscountType,
                MaxPriceDiscount = voucherCreateDto.MaxPriceDiscount,
                ValidFrom = voucherCreateDto.ValidFrom,
                ValidTo = voucherCreateDto.ValidTo,
                MaxUsagePerUser = voucherCreateDto.MaxUsagePerUser,
                TotalQuantity = voucherCreateDto.TotalQuantity ?? 0,
                MinOrderPrice = voucherCreateDto.MinOrderPrice ?? 0,
                IsDeleted = false
            };

            voucherRepository.Add(voucher);
            await uow.SaveChangesAsync(token);
            return Result.Created;
        }

        public async Task<ErrorOr<Deleted>> DeleteVoucherByGuidAsync(Guid voucherGuid, CancellationToken token)
        {
            var voucher = await voucherRepository
                .GetVoucherByGuid(voucherGuid)
                .Where(v => !v.IsDeleted)
                .FirstOrDefaultAsync();
            if (voucher == null)
            {
                return Error.NotFound(
                    "VOUCHER_NOT_FOUND",
                    "The voucher with the specified GUID does not exist."
                );
            }
            // Soft delete logic
            voucher.IsDeleted = true;
            voucher.UpdatedAt = DateTime.UtcNow;
            voucherRepository.Update(voucher);
            await uow.SaveChangesAsync(token);
            return Result.Deleted;
        }

        public async Task<ErrorOr<Deleted>> DeleteVoucherExpireAsync(CancellationToken token)
        {
            var vouchersToDelete = voucherRepository
                .GetAllVouchers()
                .Where(v => v.ValidTo < DateTime.UtcNow && !v.IsDeleted)
                .ToList();
            if (!vouchersToDelete.Any())
            {
                return Error.NotFound(
                    "NO_EXPIRED_VOUCHERS",
                    "There are no expired vouchers to delete."
                );
            }
            foreach (var voucher in vouchersToDelete)
            {
                voucher.IsDeleted = true;
                voucher.UpdatedAt = DateTime.UtcNow;
                voucherRepository.Update(voucher);
            }
            await uow.SaveChangesAsync(token);
            return Result.Deleted;
        }

        public async Task<ErrorOr<PageResult<ResponseVoucherUserDto>>> GetAllVoucherForUserAsync(Guid UserGuid, CancellationToken token)
        {
            var vouchers = await userVoucherRepository
                .GetVouchersByUserGuid(UserGuid)
                .Where(v => !v.Voucher.IsDeleted && v.Voucher.ValidTo > DateTime.UtcNow)
                .Select(v => new ResponseVoucherUserDto
                {
                    VoucherGuid = v.Voucher.PublicId,
                    VoucherName = v.Voucher.VoucherName ?? string.Empty,
                    Description = v.Voucher.VoucherDescription ?? string.Empty,
                    Discount = v.Voucher.Discount,
                    ValidFrom = v.Voucher.ValidFrom,
                    ValidTo = v.Voucher.ValidTo
                })
                .ToListAsync(token);

            if (!vouchers.Any())
            {
                return Error.NotFound(
                    "NO_VOUCHERS_FOUND",
                    "No vouchers were found for the user."
                );
            }

            var result = new PageResult<ResponseVoucherUserDto>
            {
                Items = vouchers,
                TotalCount = vouchers.Count
            };
            return result;
        }

        public async Task<ErrorOr<PageResult<ResponseVoucherAdminDto>>> GetVoucherForAdminAsync(CancellationToken token)
        {
            var vouchers = await voucherRepository
                .GetAllVouchers()
                .Where(v => !v.IsDeleted)
                .Select(v => new ResponseVoucherAdminDto
                {
                    VoucherGuid = v.PublicId,
                    VoucherName = v.VoucherName,
                    Discount = v.Discount,
                    VoucherScope = (int)v.VoucherScope,
                    DiscountType = (int)v.DiscountType,
                    MaxPriceDiscount = v.MaxPriceDiscount,
                    ValidFrom = v.ValidFrom,
                    ValidTo = v.ValidTo,
                    MinOrderPrice = v.MinOrderPrice
                })
                .ToListAsync();
            if (vouchers == null || !vouchers.Any())
            {
                return Error.NotFound(
                    "NO_VOUCHERS_FOUND",
                    "No vouchers were found in the system."
                );
            }

            var pageResult = new PageResult<ResponseVoucherAdminDto>
            {
                Items = vouchers,
                TotalCount = vouchers.Count
            };
            return pageResult;
        }

        public async Task<ErrorOr<Success>> NotifyUserAboutNewVoucherAsync(string adminEmail, string voucherName, DateTime validTo, CancellationToken token)
        {
            var users = await userRepository
                .GetAllUsers()
                .Where(u => u.Email != adminEmail)
                .ToListAsync(token);

            foreach (var user in users)
            {
                if (user.Email != adminEmail)
                {
                    return Error.NotFound(
                        "USER_NOT_FOUND",
                        "The user with the specified email does not exist."
                    );
                }
                string emailBody = $@"
                    Hi {user.UserName},

                    Great news! A new voucher has been added to your account:

                    🎁 {voucherName.ToUpper()}
                    📅 Valid until: {validTo:MMMM dd, yyyy}

                    Check your wallet and start shopping now to enjoy your discount!

                    Best regards,
                    Shoe Store Team";

                await emailService.SendEmailAsync(
                    from: adminEmail,
                    to: user.Email,
                    subject: "🎁 New Voucher Received!",
                    body: emailBody,
                    token: token
                );
            }
            return Result.Success;
        }

        public async Task<ErrorOr<Updated>> UpdateVoucherAsync(Guid voucherGuid, UpdateVoucherDto voucherUpdateDto, CancellationToken token)
        {
            var voucher = await voucherRepository
                .GetVoucherByGuid(voucherGuid)
                .FirstOrDefaultAsync();

            if (voucher == null)
            {
                return Error.NotFound(
                    "VOUCHER_NOT_FOUND",
                    "The voucher with the specified GUID does not exist."
                );
            }

            voucher.VoucherDescription = voucherUpdateDto.VoucherDescription ?? voucher.VoucherDescription;

            voucher.Discount = voucherUpdateDto.Discount.HasValue ? voucherUpdateDto.Discount.Value : voucher.Discount;

            voucher.VoucherScope = (VoucherScope)(voucherUpdateDto.VoucherScope ?? (int)voucher.VoucherScope);
            voucher.DiscountType = (DiscountType)(voucherUpdateDto.DiscountType ?? (int)voucher.DiscountType);

            voucher.MaxPriceDiscount = voucherUpdateDto.MaxPriceDiscount ?? voucher.MaxPriceDiscount;

            voucher.ValidFrom = voucherUpdateDto.ValidFrom ?? voucher.ValidFrom;
            voucher.ValidTo = voucherUpdateDto.ValidTo ?? voucher.ValidTo;

            voucher.MaxUsagePerUser = voucherUpdateDto.MaxUsagePerUser ?? voucher.MaxUsagePerUser;
            voucher.TotalQuantity = voucherUpdateDto.TotalQuantity ?? voucher.TotalQuantity;
            voucher.MinOrderPrice = voucherUpdateDto.MinOrderPrice ?? voucher.MinOrderPrice;

            voucher.UpdatedAt = DateTime.UtcNow;

            voucherRepository.Update(voucher);
            await uow.SaveChangesAsync(token);

            return Result.Updated;
            
        }

    }
}
