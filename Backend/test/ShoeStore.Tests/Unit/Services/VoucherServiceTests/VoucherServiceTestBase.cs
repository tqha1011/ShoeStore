using MockQueryable;
using Moq;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.Notification;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Tests.Unit.Services.VoucherServiceTests;

public abstract class VoucherServiceTestBase
{
    protected Mock<INotificationQueue> QueueMock { get; } = new();
    protected Mock<IUnitOfWork> UowMock { get; } = new();
    protected Mock<IUserRepository> UserRepositoryMock { get; } = new();
    protected Mock<IVoucherRepository> VoucherRepositoryMock { get; } = new();

    protected VoucherServiceTestBase()
    {
        Service = new VoucherService(
            QueueMock.Object,
            UowMock.Object,
            UserRepositoryMock.Object,
            VoucherRepositoryMock.Object);
    }

    protected VoucherService Service { get; }

    protected void VerifyDatabaseSafe(Func<Times> addTimes, Func<Times> saveTimes)
    {
        VoucherRepositoryMock.Verify(x => x.Add(It.IsAny<Voucher>()), addTimes);
        UowMock.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), saveTimes);
    }

    protected void VerifyQueueSafe(Func<Times> enqueueTimes)
    {
        QueueMock.Verify(x => x.EnqueueAsync(It.IsAny<VoucherNotificationDto>(), It.IsAny<CancellationToken>()), enqueueTimes);
    }

    protected static IQueryable<T> AsAsyncQueryable<T>(IEnumerable<T> source) where T : class
    {
        return source.ToList().BuildMock().AsQueryable();
    }

    protected static CreateVoucherDto CreateCreateVoucherDto()
    {
        return new CreateVoucherDto
        {
            VoucherName = "SPRING-SALE",
            VoucherDescription = "Spring voucher",
            Discount = 15,
            VoucherScope = VoucherScope.Product,
            DiscountType = DiscountType.Percentage,
            MaxPriceDiscount = 100,
            ValidFrom = DateTime.UtcNow.AddDays(-1),
            ValidTo = DateTime.UtcNow.AddDays(30),
            MaxUsagePerUser = 1,
            TotalQuantity = 50,
            MinOrderPrice = 200
        };
    }

    protected static UpdateVoucherDto CreateUpdateVoucherDto()
    {
        return new UpdateVoucherDto
        {
            VoucherDescription = "Updated voucher",
            Discount = 20,
            VoucherScope = VoucherScope.Shipping,
            DiscountType = DiscountType.FixedAmount,
            MaxPriceDiscount = 80,
            ValidFrom = DateTime.UtcNow,
            ValidTo = DateTime.UtcNow.AddDays(20),
            MaxUsagePerUser = 2,
            TotalQuantity = 99,
            MinOrderPrice = 150
        };
    }

    protected static Voucher CreateVoucher(Guid publicId, bool isDeleted = false)
    {
        return new Voucher
        {
            Id = 1,
            PublicId = publicId,
            VoucherName = "OLD-VOUCHER",
            VoucherDescription = "Old desc",
            Discount = 10,
            VoucherScope = VoucherScope.Product,
            DiscountType = DiscountType.Percentage,
            MaxPriceDiscount = 50,
            ValidFrom = DateTime.UtcNow.AddDays(-10),
            ValidTo = DateTime.UtcNow.AddDays(10),
            MaxUsagePerUser = 1,
            TotalQuantity = 10,
            MinOrderPrice = 100,
            IsDeleted = isDeleted,
            CreatedAt = DateTime.UtcNow.AddDays(-2)
        };
    }

    protected static User CreateUser(int id, string email, string username, UserRole role = UserRole.User)
    {
        return new User
        {
            Id = id,
            Email = email,
            UserName = username,
            Password = "hashed-password",
            Role = role
        };
    }
}
