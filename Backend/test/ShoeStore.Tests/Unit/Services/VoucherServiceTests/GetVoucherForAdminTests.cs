using Moq;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Tests.Unit.Services.VoucherServiceTests;

public class GetVoucherForAdminTests : VoucherServiceTestBase
{
    [Fact]
    public async Task GetVoucherForAdminAsync_WhenVouchersExist_ReturnsPaginatedResult()
    {
        // Arrange
        var firstGuid = Guid.NewGuid();
        var secondGuid = Guid.NewGuid();
        var deletedGuid = Guid.NewGuid();

        var newestVoucher = CreateVoucher(firstGuid);
        newestVoucher.CreatedAt = DateTime.UtcNow.AddDays(-1);
        newestVoucher.VoucherName = "NEWEST";

        var olderVoucher = CreateVoucher(secondGuid);
        olderVoucher.CreatedAt = DateTime.UtcNow.AddDays(-2);
        olderVoucher.VoucherName = "OLDER";

        var deletedVoucher = CreateVoucher(deletedGuid, true);
        deletedVoucher.CreatedAt = DateTime.UtcNow.AddDays(-3);
        deletedVoucher.VoucherName = "DELETED";

        var vouchers = new List<Voucher>
        {
            newestVoucher,
            olderVoucher,
            deletedVoucher
        };

        VoucherRepositoryMock.Setup(x => x.GetAllVouchers(false))
            .Returns(AsAsyncQueryable(vouchers));

        // Act
        var result = await Service.GetVoucherForAdminAsync(CancellationToken.None, pageIndex: 1, pageSize: 2);

        // Assert
        Assert.False(result.IsError);
        Assert.Equal(2, result.Value.TotalCount);
        Assert.Equal(1, result.Value.PageNumber);
        Assert.Equal(2, result.Value.PageSize);

        var items = result.Value.Items.ToList();
        Assert.Equal(2, items.Count);
        Assert.Equal("NEWEST", items[0].VoucherName);
        Assert.Equal("OLDER", items[1].VoucherName);

        VerifyDatabaseSafe(Times.Never, Times.Never);
        VerifyQueueSafe(Times.Never);
    }

    [Fact]
    public async Task GetVoucherForAdminAsync_WhenRepositoryThrows_ThrowsException()
    {
        // Arrange
        VoucherRepositoryMock.Setup(x => x.GetAllVouchers(false))
            .Throws(new InvalidOperationException("Database unavailable"));

        // Act
        var act = async () => await Service.GetVoucherForAdminAsync(CancellationToken.None);

        // Assert
        await Assert.ThrowsAsync<InvalidOperationException>(act);
        VerifyDatabaseSafe(Times.Never, Times.Never);
        VerifyQueueSafe(Times.Never);
    }
}
