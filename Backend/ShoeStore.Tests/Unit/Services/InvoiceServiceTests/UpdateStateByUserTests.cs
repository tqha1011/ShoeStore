using Moq;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Tests.Unit.Services.InvoiceServiceTests;

public class UpdateStateByUserTests
{
    private readonly Mock<ICurrentUser> _currentUser = new();

    // generate mock data by using Moq nuget
    private readonly Mock<IInvoiceRepository> _mockRepo = new();
    private readonly Mock<IUnitOfWork> _mockUow = new();

    private readonly InvoiceService _updateStateByUser;

    public UpdateStateByUserTests()
    {
        _updateStateByUser = new InvoiceService(_mockRepo.Object, _mockUow.Object, _currentUser.Object);
    }


    [Theory]
    // SePay (paymentId = 1)
    [InlineData(1, InvoiceStatus.Cancelled, InvoiceStatus.Paid)]
    [InlineData(1, InvoiceStatus.Cancelled, InvoiceStatus.Delivering)]
    [InlineData(1, InvoiceStatus.Cancelled, InvoiceStatus.Pending)]
    [InlineData(1, InvoiceStatus.Delivering, InvoiceStatus.Pending)]
    [InlineData(1, InvoiceStatus.Paid, InvoiceStatus.Pending)]
    // COD (paymentId = 2)
    [InlineData(2, InvoiceStatus.Cancelled, InvoiceStatus.Paid)]
    [InlineData(2, InvoiceStatus.Cancelled, InvoiceStatus.Delivering)]
    [InlineData(2, InvoiceStatus.Cancelled, InvoiceStatus.Pending)]
    [InlineData(2, InvoiceStatus.Pending, InvoiceStatus.Paid)]
    [InlineData(2, InvoiceStatus.Delivering, InvoiceStatus.Pending)]
    [InlineData(2, InvoiceStatus.Paid, InvoiceStatus.Pending)]
    public async Task UpdateInvoiceStateByUser_WhenChangeToInvalidStatus_ReturnValidation(
        int paymentId,
        InvoiceStatus currStatus,
        InvoiceStatus newStatus)
    {
        var fakeGuid = Guid.NewGuid();
        var fakeUser = new User
        {
            Id = 1,
            UserName = "testuser",
            Password = "testpass",
            PublicId = Guid.NewGuid(),
            Email = "test@gmail.com"
        };
        var fakeRequest = new UpdateStateRequestDto
        {
            Status = newStatus
        };

        var fakeInvoice = new Invoice
        {
            Id = 1,
            UserId = 1,
            User = fakeUser,
            FullName = "Test User",
            Status = currStatus,
            Phone = "",
            PaymentId = paymentId,
            ShippingAddress = "",
            FinalPrice = 100,
            OrderCode = "DH123"
        };

        _mockRepo.Setup(repo => repo.GetByPublicIdAsync(fakeGuid, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeInvoice);

        var result =
            await _updateStateByUser.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("Invoice.Forbidden", result.FirstError.Code);

        VerifySafeDatabase(Times.Never);
    }

    [Fact]
    public async Task UpdateInvoiceStateByUser_WhenInvoiceDoesNotExist_ReturnNotFound()
    {
        // unit test include 3 stages : Arrange, Action, Assert
        // Stage 1: Arrange
        var fakeGuid = Guid.NewGuid();
        var fakeRequest = new UpdateStateRequestDto
        {
            Status = InvoiceStatus.Paid
        };

        // set up the invoice is not exist in database
        _mockRepo.Setup(repo => repo.GetByPublicIdAsync(fakeGuid, It.IsAny<CancellationToken>()))
            .ReturnsAsync((Invoice?)null);

        // stage 2: Action
        var adminResult =
            await _updateStateByUser.UpdateInvoiceStateByUserAsync(fakeGuid, fakeRequest, CancellationToken.None);


        // stage 3: Assert
        Assert.True(adminResult.IsError);
        Assert.Equal("Invoice.NotFound", adminResult.FirstError.Code);

        VerifySafeDatabase(Times.Never);
    }

    [Fact]
    public async Task UpdateInvoiceStateByUser_WhenUserIsNotOwner_ReturnUnauthorized()
    {
        var fakeGuid = Guid.NewGuid();
        var fakeId = Guid.NewGuid();
        _currentUser.Setup(cu => cu.Id).Returns(fakeId);
        var fakeRequest = new UpdateStateRequestDto
        {
            Status = InvoiceStatus.Cancelled
        };

        var fakeUser = new User
        {
            Id = 2,
            UserName = "testuser2",
            Password = "testpass2",
            PublicId = Guid.NewGuid(),
            Email = ""
        };

        var fakeInvoice = new Invoice
        {
            Id = 1,
            UserId = 1,
            FullName = "Test User",
            User = fakeUser,
            Status = InvoiceStatus.Pending,
            Phone = "",
            PaymentId = 1,
            ShippingAddress = "",
            FinalPrice = 100,
            OrderCode = "DH123"
        };

        _mockRepo.Setup(repo => repo.GetByPublicIdAsync(fakeGuid, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeInvoice);

        var result =
            await _updateStateByUser.UpdateInvoiceStateByUserAsync(fakeGuid, fakeRequest, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("User.Unauthorized", result.FirstError.Code);

        VerifySafeDatabase(Times.Never);
    }

    private void VerifySafeDatabase(Func<Times> times)
    {
        _mockRepo.Verify(repo => repo.Update(It.IsAny<Invoice>()), times);
        _mockUow.Verify(uow => uow.SaveChangesAsync(It.IsAny<CancellationToken>()), times);
    }
}