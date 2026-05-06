using Microsoft.Data.Sqlite;
using Microsoft.EntityFrameworkCore;
using Moq;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.Notification;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;
using ShoeStore.Infrastructure.Data;
using ShoeStore.Infrastructure.Repositories;

namespace ShoeStore.Tests.Unit.Services.VoucherServiceTests;

public class DeleteVoucherExpireTests
{
    [Fact]
    public async Task DeleteVoucherExpireAsync_WhenExpiredVoucherExists_ReturnsDeletedAndSoftDeletesVoucher()
    {
        // Arrange
        await using var scope = await CreateTestScopeAsync();
        var expiredVoucher = CreateVoucher(DateTime.UtcNow.AddDays(-1));
        var unexpiredVoucher = CreateVoucher(DateTime.UtcNow.AddDays(1));

        scope.Context.Vouchers.Add(expiredVoucher);
        scope.Context.Vouchers.Add(unexpiredVoucher);
        await scope.Context.SaveChangesAsync();

        // Act
        var result = await scope.Service.DeleteVoucherExpireAsync(CancellationToken.None);

        // Assert
        Assert.False(result.IsError);

        var persistedVoucher = await scope.Context.Vouchers
            .IgnoreQueryFilters()
            .AsNoTracking()
            .SingleAsync(x => x.VoucherName == expiredVoucher.VoucherName);
        
        var activeVoucher = await scope.Context.Vouchers
            .IgnoreQueryFilters()
            .AsNoTracking()
            .SingleAsync(x => x.VoucherName == unexpiredVoucher.VoucherName);

        Assert.True(persistedVoucher.IsDeleted);
        Assert.False(activeVoucher.IsDeleted);

        scope.UowMock.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
        scope.QueueMock.Verify(
            x => x.EnqueueAsync(It.IsAny<VoucherNotificationDto>(), It.IsAny<CancellationToken>()),
            Times.Never);
    }

    [Fact]
    public async Task DeleteVoucherExpireAsync_WhenTokenIsCancelled_ThrowsOperationCanceledException()
    {
        // Arrange
        await using var scope = await CreateTestScopeAsync();
        using var cancellation = new CancellationTokenSource();
        await cancellation.CancelAsync();

        // Act
        var act = async () => await scope.Service.DeleteVoucherExpireAsync(cancellation.Token);

        // Assert
        await Assert.ThrowsAnyAsync<OperationCanceledException>(act);
        scope.UowMock.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
        scope.QueueMock.Verify(
            x => x.EnqueueAsync(It.IsAny<VoucherNotificationDto>(), It.IsAny<CancellationToken>()),
            Times.Never);
    }

    private static async Task<TestScope> CreateTestScopeAsync()
    {
        var connection = new SqliteConnection("Data Source=:memory:");
        await connection.OpenAsync();

        var options = new DbContextOptionsBuilder<AppDbContext>()
            .UseSqlite(connection)
            .Options;

        var context = new AppDbContext(options);
        await context.Database.EnsureCreatedAsync();

        var repository = new VoucherRepository(context);
        var queueMock = new Mock<INotificationQueue>();
        var uowMock = new Mock<IUnitOfWork>();
        var userRepositoryMock = new Mock<IUserRepository>();
        var service = new VoucherService(queueMock.Object, uowMock.Object, userRepositoryMock.Object, repository);

        return new TestScope(connection, context, service, queueMock, uowMock);
    }

    private static Voucher CreateVoucher(DateTime validTo, bool isDeleted = false)
    {
        return new Voucher
        {
            VoucherName = $"EXPIRE-TEST-{Guid.NewGuid():N}",
            VoucherDescription = "Expire test voucher",
            Discount = 10,
            VoucherScope = VoucherScope.Product,
            DiscountType = DiscountType.Percentage,
            MaxPriceDiscount = 100,
            ValidFrom = DateTime.UtcNow.AddDays(-5),
            ValidTo = validTo,
            MaxUsagePerUser = 1,
            TotalQuantity = 100,
            MinOrderPrice = 0,
            IsDeleted = isDeleted
        };
    }

    private sealed class TestScope(
        SqliteConnection connection,
        AppDbContext context,
        VoucherService service,
        Mock<INotificationQueue> queueMock,
        Mock<IUnitOfWork> uowMock) : IAsyncDisposable
    {
        public AppDbContext Context { get; } = context;
        public VoucherService Service { get; } = service;
        public Mock<INotificationQueue> QueueMock { get; } = queueMock;
        public Mock<IUnitOfWork> UowMock { get; } = uowMock;

        public async ValueTask DisposeAsync()
        {
            await Context.DisposeAsync();
            await connection.DisposeAsync();
        }
    }
}
