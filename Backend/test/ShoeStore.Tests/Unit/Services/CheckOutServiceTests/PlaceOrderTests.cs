using System.Data;
using ErrorOr;
using Microsoft.EntityFrameworkCore;
using Moq;
using ShoeStore.Application.DTOs.CheckOutDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Tests.Unit.Services.CheckOutServiceTests;

public class PlaceOrderTests
{
    private readonly Mock<ICartItemRepository> _cartItemRepository = new();
    private readonly CheckOutService _checkOutService;

    private readonly Mock<IInvoiceRepository> _invoiceRepository = new();

    private readonly Mock<IUnitOfWork> _mockUow = new();

    private readonly Mock<IProductVariantRepository> _productVariantRepository = new();
    private readonly Mock<IDbTransaction> _transaction = new();

    private readonly Mock<IUserRepository> _userRepository = new();

    public PlaceOrderTests()
    {
        _checkOutService = new CheckOutService(_productVariantRepository.Object, _mockUow.Object,
            _cartItemRepository.Object, _invoiceRepository.Object, _userRepository.Object);
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenUserIsNotExist_ReturnNotFound()
    {
        // Arrange
        ArrangeExecutionStrategy();
        ArrangeTransaction();

        var fakeUserId = Guid.NewGuid();
        var fakeCheckOutRequest = new CheckOutRequestDto
        (
            Guid.NewGuid()
        );

        List<CheckOutRequestDto> fakeItems = [fakeCheckOutRequest];

        var fakePlaceOrderRequest = new PlaceOrderRequestDto(fakeItems, [], "", "", 1, "");

        _userRepository.Setup(x => x.GetUserByPublicIdAsync(fakeUserId, It.IsAny<CancellationToken>()))
            .ReturnsAsync((User?)null);

        // Act
        var result =
            await _checkOutService.PlaceOrderAsync(fakePlaceOrderRequest, fakeUserId, false, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("User.NotFound", result.FirstError.Code);

        _invoiceRepository.Verify(repo => repo.Add(It.IsAny<Invoice>()), Times.Never);
        VerifyUnitOfWorkCalls(_mockUow, Times.Never, Times.Never, Times.Never);
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenVariantDoesNotExist_ReturnNotFound()
    {
        // Arrange
        ArrangeExecutionStrategy();
        ArrangeTransaction();

        var fakeUserId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        var request = new PlaceOrderRequestDto([new CheckOutRequestDto(variantId)], [], "A", "HCM", 1, "0123");

        var user = BuildUser(1, fakeUserId);

        _userRepository.Setup(x => x.GetUserByPublicIdAsync(fakeUserId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(user);
        _productVariantRepository
            .Setup(x => x.GetListVariantsAsync(It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([]);

        // Act
        var result = await _checkOutService.PlaceOrderAsync(request, fakeUserId, false, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Variant.NotFound", result.FirstError.Code);

        _invoiceRepository.Verify(repo => repo.Add(It.IsAny<Invoice>()), Times.Never);
        VerifyUnitOfWorkCalls(_mockUow, Times.Never, Times.Never, Times.Never);
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenStockIsNotEnough_ReturnValidationError()
    {
        // Arrange
        ArrangeExecutionStrategy();
        ArrangeTransaction();

        var fakeUserId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        var request = new PlaceOrderRequestDto([new CheckOutRequestDto(variantId, 3)], [], "A", "Da Nang", 1, "0123");

        var user = BuildUser(1, fakeUserId);
        var variant = BuildVariant(1, variantId, 2, 100);

        _userRepository.Setup(x => x.GetUserByPublicIdAsync(fakeUserId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(user);
        _productVariantRepository
            .Setup(x => x.GetListVariantsAsync(It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([variant]);

        // Act
        var result = await _checkOutService.PlaceOrderAsync(request, fakeUserId, false, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Stock.NotEnough", result.FirstError.Code);

        _invoiceRepository.Verify(repo => repo.Add(It.IsAny<Invoice>()), Times.Never);
        VerifyUnitOfWorkCalls(_mockUow, Times.Never, Times.Never, Times.Never);
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenSaveChangesThrowsConcurrency_ReturnConflictAndRollback()
    {
        // Arrange
        ArrangeExecutionStrategy();
        ArrangeTransaction();

        var fakeUserId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        var request = new PlaceOrderRequestDto([new CheckOutRequestDto(variantId)], [], "John", "Ha Noi", 1, "0909");

        var user = BuildUser(1, fakeUserId);
        var variant = BuildVariant(1, variantId, 5, 100);

        _userRepository.Setup(x => x.GetUserByPublicIdAsync(fakeUserId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(user);
        _productVariantRepository
            .Setup(x => x.GetListVariantsAsync(It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([variant]);
        _mockUow.Setup(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()))
            .ThrowsAsync(new DbUpdateConcurrencyException());

        // Act
        var result = await _checkOutService.PlaceOrderAsync(request, fakeUserId, false, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Checkout.Concurrency", result.FirstError.Code);

        _invoiceRepository.Verify(repo => repo.Add(It.IsAny<Invoice>()), Times.Once);
        VerifyUnitOfWorkCalls(_mockUow, Times.Once, Times.Never, Times.Once);
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenRequestIsValid_ReturnInvoiceDtoAndCommitTransaction()
    {
        // Arrange
        ArrangeExecutionStrategy();
        ArrangeTransaction();

        var fakeUserId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        var request = new PlaceOrderRequestDto([new CheckOutRequestDto(variantId, 4)], [], "John", "Ha Noi", 1, "0909");

        var user = BuildUser(1, fakeUserId, [
            new CartItem
            {
                Id = 10,
                UserId = 1,
                Quantity = 4,
                ProductVariantId = 1,
                ProductVariant = new ProductVariant
                {
                    Id = 1,
                    ProductId = 1,
                    SizeId = 1,
                    ColorId = 1,
                    Stock = 5,
                    IsSelling = true,
                    Price = 100,
                    PublicId = variantId
                }
            }
        ]);

        var variant = BuildVariant(1, variantId, 5, 100);
        Invoice? addedInvoice = null;

        _userRepository.Setup(x => x.GetUserByPublicIdAsync(fakeUserId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(user);
        _productVariantRepository
            .Setup(x => x.GetListVariantsAsync(It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([variant]);
        _invoiceRepository.Setup(x => x.Add(It.IsAny<Invoice>()))
            .Callback<Invoice>(invoice => addedInvoice = invoice);

        // Act
        var result = await _checkOutService.PlaceOrderAsync(request, fakeUserId, true, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        Assert.Equal("John", result.Value.FullName);
        Assert.Equal("Ha Noi", result.Value.ShippingAddress);
        Assert.Equal("0909", result.Value.PhoneNumber);
        Assert.Equal(InvoiceStatus.Pending, result.Value.Status);
        Assert.Equal(1, variant.Stock);
        Assert.Single(result.Value.Details);

        var detail = Assert.Single(result.Value.Details);
        Assert.Equal(1, detail.ProductVariantId);
        Assert.Equal(4, detail.Quantity);
        Assert.Equal(100, detail.UnitPrice);

        Assert.NotNull(addedInvoice);
        _cartItemRepository.Verify(
            repo => repo.DeleteListCartItem(It.Is<IEnumerable<CartItem>>(items => items.Count() == 1)), Times.Once);
        _invoiceRepository.Verify(repo => repo.Add(It.IsAny<Invoice>()), Times.Once);
        VerifyUnitOfWorkCalls(_mockUow, Times.Once, Times.Once, Times.Never);
    }

    private void ArrangeExecutionStrategy()
    {
        _mockUow.Setup(uow => uow.ExecuteWithStrategyAsync(It.IsAny<Func<Task<ErrorOr<InvoiceDto>>>>(),
                It.IsAny<CancellationToken>()))
            .Returns((Func<Task<ErrorOr<InvoiceDto>>> action, CancellationToken _) => action());
    }

    private void ArrangeTransaction()
    {
        _mockUow.Setup(uow => uow.BeginTransactionAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(_transaction.Object);
    }

    private static void VerifyUnitOfWorkCalls(
        Mock<IUnitOfWork> mockUow,
        Func<Times> saveChangesTimes,
        Func<Times> commitTimes,
        Func<Times> rollbackTimes)
    {
        mockUow.Verify(uow => uow.SaveChangesAsync(It.IsAny<CancellationToken>()), saveChangesTimes);
        mockUow.Verify(uow => uow.CommitTransactionAsync(It.IsAny<CancellationToken>()), commitTimes);
        mockUow.Verify(uow => uow.RollbackTransactionAsync(It.IsAny<CancellationToken>()), rollbackTimes);
    }

    private static User BuildUser(int id, Guid publicId, List<CartItem>? cartItems = null)
    {
        return new User
        {
            Id = id,
            PublicId = publicId,
            UserName = "test-user",
            Password = "hashed-password",
            Email = "test@email.com",
            CartItems = cartItems ?? []
        };
    }

    private static ProductVariant BuildVariant(int id, Guid publicId, int stock, decimal price)
    {
        return new ProductVariant
        {
            Id = id,
            PublicId = publicId,
            ProductId = 1,
            SizeId = 1,
            ColorId = 1,
            Stock = stock,
            IsSelling = true,
            Price = price,
            Product = new Product
            {
                Id = 1,
                ProductName = "Jordan"
            }
        };
    }
}