using Moq;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Domain.Enum;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Tests.Unit.Services.VoucherServiceTests;

public class CreateVoucherServiceTests : VoucherServiceTestBase
{
    [Fact]
    public async Task CreateVoucherAsync_WhenRequestIsValid_ReturnsCreatedAndPersistsVoucher()
    {
        // Arrange
        var dto = CreateCreateVoucherDto();
        var users = new List<User>
        {
            CreateUser(1, "user1@test.com", "user1"),
            CreateUser(2, "admin@test.com", "admin", UserRole.Admin)
        };
        VoucherNotificationDto? queuedNotification = null;

        UserRepositoryMock.Setup(x => x.GetAllUsers())
            .Returns(AsAsyncQueryable(users));
        QueueMock.Setup(x => x.EnqueueAsync(It.IsAny<VoucherNotificationDto>(), It.IsAny<CancellationToken>()))
            .Callback<VoucherNotificationDto, CancellationToken>((notification, _) => queuedNotification = notification)
            .Returns(ValueTask.CompletedTask);

        Voucher? addedVoucher = null;
        VoucherRepositoryMock.Setup(x => x.Add(It.IsAny<Voucher>()))
            .Callback<Voucher>(voucher => addedVoucher = voucher);

        // Act
        var result = await Service.CreateVoucherAsync(dto, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        var actualVoucher = Assert.IsType<Voucher>(addedVoucher);
        Assert.Equal(dto.VoucherName, actualVoucher.VoucherName);
        Assert.Equal(dto.Discount, actualVoucher.Discount);
        Assert.Equal(dto.ValidTo, actualVoucher.ValidTo);

        var actualNotification = Assert.IsType<VoucherNotificationDto>(queuedNotification);
        Assert.Equal(actualVoucher.Id, actualNotification.VoucherId);
        Assert.Equal(actualVoucher.VoucherName, actualNotification.VoucherName);
        Assert.Single(actualNotification.TargetUsers);
        Assert.Equal("user1@test.com", actualNotification.TargetUsers[0].Email);

        VerifyDatabaseSafe(Times.Once, Times.Once);
        VerifyQueueSafe(Times.Once);
    }

    [Fact]
    public async Task CreateVoucherAsync_WhenQueueThrows_ThrowsException()
    {
        // Arrange
        var dto = CreateCreateVoucherDto();
        var users = new List<User>
        {
            CreateUser(1, "user1@test.com", "user1")
        };

        UserRepositoryMock.Setup(x => x.GetAllUsers())
            .Returns(AsAsyncQueryable(users));
        QueueMock.Setup(x => x.EnqueueAsync(It.IsAny<VoucherNotificationDto>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(new InvalidOperationException("Queue unavailable"));

        // Act
        var act = async () => await Service.CreateVoucherAsync(dto, CancellationToken.None);

        // Assert
        await Assert.ThrowsAsync<InvalidOperationException>(act);
        VerifyDatabaseSafe(Times.Once, Times.Once);
        VerifyQueueSafe(Times.Once);
    }
}