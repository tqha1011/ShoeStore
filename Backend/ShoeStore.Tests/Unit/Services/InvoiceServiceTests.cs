using Moq;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Tests.Unit.Services;

public class InvoiceServiceTests
{
    private readonly Mock<ICurrentUser> _currentUser = new();

    private readonly InvoiceService _invoiceService;

    // generate mock data by using Moq nuget
    private readonly Mock<IInvoiceRepository> _mockRepo = new();
    private readonly Mock<IUnitOfWork> _mockUow = new();

    public InvoiceServiceTests()
    {
        _invoiceService = new InvoiceService(_mockRepo.Object, _mockUow.Object, _currentUser.Object);
    }

    [Fact]
    public async Task UpdateInvoiceStateByAdmin_WhenInvoiceDoesNotExist_ReturnNotFound()
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
            await _invoiceService.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);


        // stage 3: Assert
        Assert.True(adminResult.IsError);
        Assert.Equal("Invoice.NotFound", adminResult.FirstError.Code);
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
    public async Task UpdateInvoiceStateByAdmin_WhenChangeStatusIsInvalidForOnlinePayment_ReturnForbidden(
        int paymentId,
        InvoiceStatus currStatus,
        InvoiceStatus newStatus)
    {
        // Arrange
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
            FinalPrice = 100
        };

        _mockRepo.Setup(repo => repo.GetByPublicIdAsync(fakeGuid, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeInvoice);

        // step 2: Action
        var result =
            await _invoiceService.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);

        // step 3: Assert
        Assert.True(result.IsError);
        Assert.Equal("Invoice.Forbidden", result.FirstError.Code);
    }

    [Theory]
    [InlineData(1, InvoiceStatus.Pending, InvoiceStatus.Paid)]
    public async Task UpdateInvoiceStateByAdmin_WhenChangeStatusToPaidWithoutPayment_ReturnValidation(
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

        List<PaymentTransaction> fakePaymentTransactions = [];

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
            PaymentTransactions = fakePaymentTransactions,
            OrderCode = "DH123"
        };

        _mockRepo.Setup(repo => repo.GetByPublicIdAsync(fakeGuid, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeInvoice);

        var result =
            await _invoiceService.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("Invoice.InvalidStatus", result.FirstError.Code);
    }

    [Theory]
    [InlineData(1, InvoiceStatus.Pending, InvoiceStatus.Paid)]
    public async Task UpdateInvoiceStateByAdmin_WhenChangeStatusToPaidWithNotEnoughPayment_ReturnValidation(
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

        List<PaymentTransaction> fakePaymentTransactions =
        [
            new()
            {
                Id = 1,
                InvoiceId = 1,
                Amount = 10,
                PaymentId = paymentId,
                OrderCode = "DH123"
            },

            new()
            {
                Id = 2,
                InvoiceId = 1,
                Amount = 30,
                PaymentId = paymentId,
                OrderCode = "DH123"
            }
        ];

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
            PaymentTransactions = fakePaymentTransactions,
            OrderCode = "DH123"
        };

        _mockRepo.Setup(repo => repo.GetByPublicIdAsync(fakeGuid, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeInvoice);

        var result =
            await _invoiceService.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("Invoice.InvalidStatus", result.FirstError.Code);
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
            await _invoiceService.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("Invoice.Forbidden", result.FirstError.Code);
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
            await _invoiceService.UpdateInvoiceStateByUserAsync(fakeGuid, fakeRequest, CancellationToken.None);


        // stage 3: Assert
        Assert.True(adminResult.IsError);
        Assert.Equal("Invoice.NotFound", adminResult.FirstError.Code);
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
        
        var result = await _invoiceService.UpdateInvoiceStateByUserAsync(fakeGuid, fakeRequest, CancellationToken.None);
        
        Assert.True(result.IsError);
        Assert.Equal("User.Unauthorized", result.FirstError.Code);
    }
}