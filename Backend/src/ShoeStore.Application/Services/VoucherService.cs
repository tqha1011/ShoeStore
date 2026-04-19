using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Application.DTOs.VoucherDtos;
using ErrorOr;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;
using Microsoft.EntityFrameworkCore;

namespace ShoeStore.Application.Services
{
    public class VoucherService : IVoucherService
    {
        private readonly IVoucherRepository repository;
        private readonly IUnitOfWork uow;

        public VoucherService(IVoucherRepository repository, IUnitOfWork uow)
        {
            this.repository = repository;
            this.uow = uow;
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

            repository.Add(voucher);
            await uow.SaveChangesAsync(token);
            return Result.Created;
            
        }

        public async Task<ErrorOr<Updated>> UpdateVoucherAsync(
    Guid voucherGuid,
    UpdateVoucherDto voucherUpdateDto,
    CancellationToken token)
        {

            var voucher = await repository
                .GetVoucherByGuid(voucherGuid)
                .FirstOrDefaultAsync();

            if (voucher == null)
            {
                return Error.NotFound(
                    "VOUCHER_NOT_FOUND",
                    "The voucher with the specified GUID does not exist."
                );
            }

            // Update logic
            voucher.VoucherDescription = voucherUpdateDto.VoucherDescription ?? voucher.VoucherDescription;

            voucher.VoucherScope = (VoucherScope)voucherUpdateDto.VoucherScope;
            voucher.DiscountType = (DiscountType)voucherUpdateDto.DiscountType;

            voucher.MaxPriceDiscount = voucherUpdateDto.MaxPriceDiscount;

            voucher.ValidFrom = voucherUpdateDto.ValidFrom ?? voucher.ValidFrom;
            voucher.ValidTo = voucherUpdateDto.ValidTo ?? voucher.ValidTo;

            voucher.MaxUsagePerUser = voucherUpdateDto.MaxUsagePerUser ?? voucher.MaxUsagePerUser;
            voucher.TotalQuantity = voucherUpdateDto.TotalQuantity ?? voucher.TotalQuantity;
            voucher.MinOrderPrice = voucherUpdateDto.MinOrderPrice ?? voucher.MinOrderPrice;

            voucher.UpdatedAt = DateTime.UtcNow;

            repository.Update(voucher);
            await uow.SaveChangesAsync(token);

            return Result.Updated;
            
        }

    }
}
