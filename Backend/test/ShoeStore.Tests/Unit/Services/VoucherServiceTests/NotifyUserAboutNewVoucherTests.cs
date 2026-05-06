using Moq;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Tests.Unit.Services.VoucherServiceTests;

public class NotifyUserAboutNewVoucherTests : VoucherServiceTestBase
{
    [Fact]
    public async Task NotifyUserAboutNewVoucherAsync_WhenUsersExist_EnqueuesNotificationForUserRoleOnly()
    {
        // Arrange
        var voucherId = 10;
        const string voucherName = "HOT-DEAL";
        var validTo = DateTime.UtcNow.AddDays(5);
        var users = new List<User>
        {
            CreateUser(1, "u1@test.com", "user1"),
            CreateUser(2, "u2@test.com", "user2"),
            CreateUser(3, "admin@test.com", "admin", UserRole.Admin)
        };
        VoucherNotificationDto? queuedNotification = null;

        UserRepositoryMock.Setup(x => x.GetAllUsers())
            .Returns(AsAsyncQueryable(users));
        QueueMock.Setup(x => x.EnqueueAsync(It.IsAny<VoucherNotificationDto>(), It.IsAny<CancellationToken>()))
            .Callback<VoucherNotificationDto, CancellationToken>((dto, _) => queuedNotification = dto)
            .Returns(ValueTask.CompletedTask);

        // Act
        var result = await Service.NotifyUserAboutNewVoucherAsync(voucherId, voucherName, validTo, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        var actualNotification = Assert.IsType<VoucherNotificationDto>(queuedNotification);
        Assert.Equal(voucherId, actualNotification.VoucherId);
        Assert.Equal(voucherName, actualNotification.VoucherName);
        Assert.Equal(validTo, actualNotification.ValidTo);
        Assert.Equal(2, actualNotification.TargetUsers.Count);
        Assert.DoesNotContain(actualNotification.TargetUsers, x => x.Email == "admin@test.com");

        VerifyDatabaseSafe(Times.Never, Times.Never);
        VerifyQueueSafe(Times.Once);
    }

    [Fact]
    public async Task NotifyUserAboutNewVoucherAsync_WhenQueueFails_ThrowsException()
    {
        // Arrange
        var voucherId = 11;
        const string voucherName = "FLASH";
        var validTo = DateTime.UtcNow.AddDays(7);
        var users = new List<User>
        {
            CreateUser(1, "u1@test.com", "user1")
        };

        UserRepositoryMock.Setup(x => x.GetAllUsers())
            .Returns(AsAsyncQueryable(users));
        QueueMock.Setup(x => x.EnqueueAsync(It.IsAny<VoucherNotificationDto>(), It.IsAny<CancellationToken>()))
            .ThrowsAsync(new InvalidOperationException("Queue unavailable"));

        // Act
        var act = async () => await Service.NotifyUserAboutNewVoucherAsync(voucherId, voucherName, validTo, CancellationToken.None);

        // Assert
        await Assert.ThrowsAsync<InvalidOperationException>(act);
        VerifyDatabaseSafe(Times.Never, Times.Never);
        VerifyQueueSafe(Times.Once);
    }
}
