using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;
using Moq;

namespace ShoeStore.Tests.Unit.Services.VoucherServiceTests;

public class UpdateVoucherTests : VoucherServiceTestBase
{
    [Fact]
    public async Task UpdateVoucherAsync_WhenVoucherExists_ReturnsUpdatedAndPersistsChanges()
    {
        // Arrange
        var voucherGuid = Guid.NewGuid();
        var voucher = CreateVoucher(voucherGuid);
        var updateDto = CreateUpdateVoucherDto();

        VoucherRepositoryMock.Setup(x => x.GetVoucherByGuid(voucherGuid))
            .Returns(AsAsyncQueryable(new List<Voucher> { voucher }));

        // Act
        var result = await Service.UpdateVoucherAsync(voucherGuid, updateDto, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        Assert.Equal(updateDto.VoucherDescription, voucher.VoucherDescription);
        Assert.Equal(updateDto.Discount, voucher.Discount);
        Assert.Equal(VoucherScope.Shipping, voucher.VoucherScope);
        Assert.Equal(DiscountType.FixedAmount, voucher.DiscountType);
        Assert.Equal(updateDto.TotalQuantity, voucher.TotalQuantity);

        VerifyDatabaseSafe(Times.Never, Times.Once);
        VerifyQueueSafe(Times.Never);
    }

    [Fact]
    public async Task UpdateVoucherAsync_WhenVoucherNotFound_ReturnsNotFoundAndKeepsDatabaseSafe()
    {
        // Arrange
        var voucherGuid = Guid.NewGuid();
        var updateDto = CreateUpdateVoucherDto();

        VoucherRepositoryMock.Setup(x => x.GetVoucherByGuid(voucherGuid))
            .Returns(AsAsyncQueryable(new List<Voucher>()));

        // Act
        var result = await Service.UpdateVoucherAsync(voucherGuid, updateDto, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("VOUCHER_NOT_FOUND", result.FirstError.Code);

        VerifyDatabaseSafe(Times.Never, Times.Never);
        VerifyQueueSafe(Times.Never);
    }
}
