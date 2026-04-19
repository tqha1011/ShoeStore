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
        var result =
            await _invoiceService.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);
        
        // stage 3: Assert
        Assert.True(result.IsError);
        Assert.Equal("Invoice.NotFound", result.FirstError.Code);
    }

    [Fact]
    public async Task UpdateInvoiceStateByAdmin_WhenChangeStatusFromPaidToCancelled_ReturnForbidden()
    {
        // Arrange
        var fakeGuid = Guid.NewGuid();
        var fakeUser = new User
        {
            Id = 1,
            UserName = "testuser",
            Password = "testpass",
            Email = "test@gmail.com",
        };
        var fakeRequest = new UpdateStateRequestDto
        {
            Status = InvoiceStatus.Paid
        };

        var fakeInvoice = new Invoice
        {
            Id = 1,
            UserId = 1,
            User = fakeUser,
            FullName = "Test User",
            Status = InvoiceStatus.Cancelled,
            Phone = "",
            PaymentId = 1,
            ShippingAddress = "",
            FinalPrice = 100,
        };
        
        _mockRepo.Setup(repo => repo.GetByPublicIdAsync(fakeGuid, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeInvoice);
        
        // step 2: Action
        var result = await _invoiceService.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);
        
        // step 3: Assert
        Assert.True(result.IsError);
        Assert.Equal("Invoice.Forbidden", result.FirstError.Code);
    }
    
    [Fact]
    public async Task UpdateInvoiceStateByAdmin_WhenChangeStatusFromCancelledToDelivering_ReturnForbidden()
    {
        // Arrange
        var fakeGuid = Guid.NewGuid();
        var fakeUser = new User
        {
            Id = 1,
            UserName = "testuser",
            Password = "testpass",
            Email = "test@gmail.com",
        };
        var fakeRequest = new UpdateStateRequestDto
        {
            Status = InvoiceStatus.Delivering
        };

        var fakeInvoice = new Invoice
        {
            Id = 1,
            UserId = 1,
            User = fakeUser,
            FullName = "Test User",
            Status = InvoiceStatus.Cancelled,
            Phone = "",
            PaymentId = 1,
            ShippingAddress = "",
            FinalPrice = 100,
        };
        
        _mockRepo.Setup(repo => repo.GetByPublicIdAsync(fakeGuid, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeInvoice);
        
        // step 2: Action
        var result = await _invoiceService.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);
        
        // step 3: Assert
        Assert.True(result.IsError);
        Assert.Equal("Invoice.Forbidden", result.FirstError.Code);
    }
    
    [Fact]
    public async Task UpdateInvoiceStateByAdmin_WhenChangeStatusFromCancelledToPending_ReturnForbidden()
    {
        // Arrange
        var fakeGuid = Guid.NewGuid();
        var fakeUser = new User
        {
            Id = 1,
            UserName = "testuser",
            Password = "testpass",
            Email = "test@gmail.com",
        };
        var fakeRequest = new UpdateStateRequestDto
        {
            Status = InvoiceStatus.Pending
        };

        var fakeInvoice = new Invoice
        {
            Id = 1,
            UserId = 1,
            User = fakeUser,
            FullName = "Test User",
            Status = InvoiceStatus.Cancelled,
            Phone = "",
            PaymentId = 1,
            ShippingAddress = "",
            FinalPrice = 100,
        };
        
        _mockRepo.Setup(repo => repo.GetByPublicIdAsync(fakeGuid, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeInvoice);
        
        // step 2: Action
        var result = await _invoiceService.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);
        
        // step 3: Assert
        Assert.True(result.IsError);
        Assert.Equal("Invoice.Forbidden", result.FirstError.Code);
    }
    
    [Fact]
    public async Task UpdateInvoiceStateByAdmin_WhenChangeStatusToPaidWithoutPayment_ReturnForbidden()
    {
        // Arrange
        var fakeGuid = Guid.NewGuid();
        var fakeUser = new User
        {
            Id = 1,
            UserName = "testuser",
            Password = "testpass",
            Email = "test@gmail.com",
        };
        var fakeRequest = new UpdateStateRequestDto
        {
            Status = InvoiceStatus.Paid
        };

        var fakeInvoice = new Invoice
        {
            Id = 1,
            UserId = 1,
            User = fakeUser,
            FullName = "Test User",
            Status = InvoiceStatus.Pending,
            Phone = "",
            PaymentId = 1,
            ShippingAddress = "",
            FinalPrice = 100,
        };
        
        _mockRepo.Setup(repo => repo.GetByPublicIdAsync(fakeGuid, It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeInvoice);
        
        // step 2: Action
        var result = await _invoiceService.UpdateInvoiceStateByAdminAsync(fakeGuid, fakeRequest, CancellationToken.None);
        
        // step 3: Assert
        Assert.True(result.IsError);
        Assert.Equal("Invoice.Forbidden", result.FirstError.Code);
    }
}