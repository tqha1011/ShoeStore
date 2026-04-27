using Moq;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Tests.Unit.Services.VoucherServiceTests;

public class DeleteVoucherByGuidTests : VoucherServiceTestBase
{
    [Fact]
    public async Task DeleteVoucherByGuidAsync_WhenVoucherExists_ReturnsDeletedAndSaves()
    {
        // Arrange
        var voucherGuid = Guid.NewGuid();
        var voucher = CreateVoucher(voucherGuid);

        VoucherRepositoryMock.Setup(x => x.GetVoucherByGuid(voucherGuid))
            .Returns(AsAsyncQueryable(new List<Voucher> { voucher }));

        // Act
        var result = await Service.DeleteVoucherByGuidAsync(voucherGuid, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        Assert.True(voucher.IsDeleted);

        VerifyDatabaseSafe(Times.Never, Times.Once);
        VerifyQueueSafe(Times.Never);
    }

    [Fact]
    public async Task DeleteVoucherByGuidAsync_WhenVoucherNotFound_ReturnsNotFoundAndKeepsDatabaseSafe()
    {
        // Arrange
        var voucherGuid = Guid.NewGuid();
        VoucherRepositoryMock.Setup(x => x.GetVoucherByGuid(voucherGuid))
            .Returns(AsAsyncQueryable(new List<Voucher>()));

        // Act
        var result = await Service.DeleteVoucherByGuidAsync(voucherGuid, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("VOUCHER_NOT_FOUND", result.FirstError.Code);

        VerifyDatabaseSafe(Times.Never, Times.Never);
        VerifyQueueSafe(Times.Never);
    }
}
