using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Application.DTOs.VoucherDtos;
using ErrorOr;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services
{
    public class VoucherService : IVoucherService
    {
        private readonly IVoucherRepository repository;
        private readonly IUnitOfWork uow;

        public VoucherService(IVoucherRepository voucherRepository, IUnitOfWork uow)
        {
            this.voucherRepository = voucherRepository;
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

        public Task<ErrorOr<Updated>> UpdateVoucherAsync(Guid voucherGuid, UpdateVoucherDto voucherUpdateDto, CancellationToken token)
        {
            throw new NotImplementedException();
        }
    }
}
