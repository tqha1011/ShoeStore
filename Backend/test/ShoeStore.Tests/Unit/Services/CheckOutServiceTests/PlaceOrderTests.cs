using System.Data;
using ErrorOr;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Moq;
using ShoeStore.Application.DTOs.CheckOutDTOs;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Tests.Unit.Services.CheckOutServiceTests;

public class PlaceOrderTests
{
    private readonly Mock<ICartItemRepository> _cartItemRepository = new();
    private readonly CheckOutService _checkOutService;
    private readonly Mock<IConfiguration> _configuration = new();

    private readonly Mock<IInvoiceRepository> _invoiceRepository = new();

    private readonly Mock<IUnitOfWork> _mockUow = new();

    private readonly Mock<IProductVariantRepository> _productVariantRepository = new();
    private readonly Mock<IDbTransaction> _transaction = new();

    private readonly Mock<IUserRepository> _userRepository = new();
    private readonly Mock<IUserVoucherRepository> _userVoucherRepository = new();

    private readonly Mock<IVoucherRepository> _voucherRepository = new();

    public PlaceOrderTests()
    {
        _checkOutService = new CheckOutService(_productVariantRepository.Object, _mockUow.Object,
            _cartItemRepository.Object, _invoiceRepository.Object, _userRepository.Object,
            _voucherRepository.Object, _configuration.Object, _userVoucherRepository.Object);
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
        _userVoucherRepository
            .Setup(x => x.GetUserVouchersByIds(It.IsAny<List<int>>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
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
        _userVoucherRepository
            .Setup(x => x.GetUserVouchersByIds(It.IsAny<List<int>>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([]);

        // Act
        var result = await _checkOutService.PlaceOrderAsync(request, fakeUserId, false, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Stock.NotEnough", result.FirstError.Code);

        _invoiceRepository.Verify(repo => repo.Add(It.IsAny<Invoice>()), Times.Never);
        VerifyUnitOfWorkCalls(_mockUow, Times.Never, Times.Never, Times.Never);
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenConcurrencyPersists_StopsAfterFiveAttempts()
    {
        // Arrange
        ArrangeExecutionStrategy();
        ArrangeTransaction();

        var fakeUserId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        var request = new PlaceOrderRequestDto([new CheckOutRequestDto(variantId)], [], "John", "Ha Noi", 1, "0909");

        var user = BuildUser(1, fakeUserId);
        _userRepository.Setup(x => x.GetUserByPublicIdAsync(fakeUserId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(user);
        _productVariantRepository
            .Setup(x => x.GetListVariantsAsync(It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(() => new List<ProductVariant> { BuildVariant(1, variantId, 5, 100) });
        _mockUow.Setup(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()))
            .ThrowsAsync(new DbUpdateConcurrencyException());
        _userVoucherRepository
            .Setup(x => x.GetUserVouchersByIds(It.IsAny<List<int>>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([]);

        // Act
        var result = await _checkOutService.PlaceOrderAsync(request, fakeUserId, false, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Checkout.Concurrency", result.FirstError.Code);

        _invoiceRepository.Verify(repo => repo.Add(It.IsAny<Invoice>()), Times.Exactly(5));
        VerifyUnitOfWorkCalls(_mockUow, () => Times.Exactly(5), Times.Never, () => Times.Exactly(5));
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenStockRemainsAfterConcurrency_CommitsUsingFreshStock()
    {
        var userId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        var stale = BuildVariant(1, variantId, 5, 100);
        var fresh = BuildVariant(1, variantId, 4, 100);
        ArrangeRetryOrder(userId, variantId, stale, fresh);
        Invoice? pending = null;
        Invoice? committed = null;
        _invoiceRepository.Setup(x => x.Add(It.IsAny<Invoice>())).Callback<Invoice>(x => pending = x);
        _mockUow.Setup(x => x.CommitTransactionAsync(It.IsAny<CancellationToken>()))
            .Callback(() => committed = pending).Returns(Task.CompletedTask);

        var result = await _checkOutService.PlaceOrderAsync(
            new PlaceOrderRequestDto([new CheckOutRequestDto(variantId, 2)], [], "John", "Ha Noi", 2, "0909"),
            userId, false, CancellationToken.None);

        Assert.False(result.IsError);
        Assert.Equal(2, fresh.Stock);
        Assert.NotNull(committed);
        Assert.Equal(2, Assert.Single(committed.InvoiceDetails).Quantity);
        VerifyUnitOfWorkCalls(_mockUow, () => Times.Exactly(2), Times.Once, Times.Once);
    }

    [Theory]
    [InlineData(0)]
    [InlineData(1)]
    public async Task PlaceOrderAsync_WhenFreshStockCannotFillBasket_ReturnsStockChangedConflict(int freshStock)
    {
        var userId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        ArrangeRetryOrder(userId, variantId, BuildVariant(1, variantId, 5, 100),
            BuildVariant(1, variantId, freshStock, 100));

        var result = await _checkOutService.PlaceOrderAsync(
            new PlaceOrderRequestDto([new CheckOutRequestDto(variantId, 2)], [], "John", "Ha Noi", 2, "0909"),
            userId, false, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal(ErrorType.Conflict, result.FirstError.Type);
        Assert.Equal("Checkout.StockChanged", result.FirstError.Code);
        VerifyUnitOfWorkCalls(_mockUow, Times.Once, Times.Never, Times.Once);
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenVoucherUnavailableAfterConcurrency_DoesNotCommit()
    {
        var userId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        ArrangeRetryOrder(userId, variantId, BuildVariant(1, variantId, 5, 100),
            BuildVariant(1, variantId, 4, 100));
        _userVoucherRepository.SetupSequence(x => x.GetUserVouchersByIds(
                It.IsAny<List<int>>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([BuildUserVoucher()]).ReturnsAsync([]);

        var result = await _checkOutService.PlaceOrderAsync(
            new PlaceOrderRequestDto([new CheckOutRequestDto(variantId)], [1], "John", "Ha Noi", 2, "0909"),
            userId, false, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("Voucher.NotFound", result.FirstError.Code);
        VerifyUnitOfWorkCalls(_mockUow, Times.Once, Times.Never, Times.Once);
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenCancelledAfterConcurrency_DoesNotRetry()
    {
        using var cancellation = new CancellationTokenSource();
        var userId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        ArrangeRetryOrder(userId, variantId, BuildVariant(1, variantId, 5, 100),
            BuildVariant(1, variantId, 4, 100));
        _mockUow.Setup(x => x.RollbackTransactionAsync(It.IsAny<CancellationToken>()))
            .Callback(cancellation.Cancel).Returns(Task.CompletedTask);

        await Assert.ThrowsAnyAsync<OperationCanceledException>(() => _checkOutService.PlaceOrderAsync(
            new PlaceOrderRequestDto([new CheckOutRequestDto(variantId)], [], "John", "Ha Noi", 2, "0909"),
            userId, false, cancellation.Token));

        VerifyUnitOfWorkCalls(_mockUow, Times.Once, Times.Never, Times.Once);
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenCartConsumedBeforeRetry_DoesNotCreateAnotherOrder()
    {
        var userId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        var stale = BuildVariant(1, variantId, 5, 100);
        ArrangeRetryOrder(userId, variantId, stale, BuildVariant(1, variantId, 4, 100));
        _userRepository.SetupSequence(x => x.GetUserByPublicIdAsync(userId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(BuildUser(1, userId,
                [new CartItem { UserId = 1, ProductVariantId = 1, ProductVariant = stale, Quantity = 1 }]))
            .ReturnsAsync(BuildUser(1, userId));

        var result = await _checkOutService.PlaceOrderAsync(
            new PlaceOrderRequestDto([new CheckOutRequestDto(variantId)], [], "John", "Ha Noi", 2, "0909"),
            userId, true, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("CartItem.NotFound", result.FirstError.Code);
        VerifyUnitOfWorkCalls(_mockUow, Times.Once, Times.Never, Times.Once);
    }

    [Fact]
    public async Task PlaceOrderAsync_WhenDatabaseFailureIsNotConcurrency_DoesNotRetry()
    {
        var userId = Guid.NewGuid();
        var variantId = Guid.NewGuid();
        ArrangeRetryOrder(userId, variantId, BuildVariant(1, variantId, 5, 100),
            BuildVariant(1, variantId, 4, 100));
        _mockUow.Setup(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()))
            .ThrowsAsync(new DbUpdateException("Unknown database failure"));

        var result = await _checkOutService.PlaceOrderAsync(
            new PlaceOrderRequestDto([new CheckOutRequestDto(variantId)], [], "John", "Ha Noi", 2, "0909"),
            userId, false, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("InvoiceCreation.Failed", result.FirstError.Code);
        VerifyUnitOfWorkCalls(_mockUow, Times.Once, Times.Never, Times.Once);
    }

    private void ArrangeRetryOrder(Guid userId, Guid variantId, ProductVariant stale, ProductVariant fresh)
    {
        ArrangeExecutionStrategy();
        ArrangeTransaction();
        _userRepository.Setup(x => x.GetUserByPublicIdAsync(userId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(() => BuildUser(1, userId));
        _productVariantRepository.SetupSequence(x => x.GetListVariantsAsync(
                It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([stale]).ReturnsAsync([fresh]);
        _userVoucherRepository.Setup(x => x.GetUserVouchersByIds(
                It.IsAny<List<int>>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([]);
        _mockUow.SetupSequence(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()))
            .ThrowsAsync(new DbUpdateConcurrencyException()).Returns(Task.CompletedTask);
    }

    private static UserVoucher BuildUserVoucher() => new()
    {
        Id = 1, UserId = 1, VoucherId = 1,
        Voucher = new Voucher
        {
            Id = 1, VoucherName = "Test voucher", Discount = 10, DiscountType = DiscountType.FixedAmount,
            MaxUsagePerUser = 1, IsDeleted = false,
            ValidFrom = DateTime.UtcNow.AddDays(-1), ValidTo = DateTime.UtcNow.AddDays(1)
        }
    };

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
        _userVoucherRepository
            .Setup(x => x.GetUserVouchersByIds(It.IsAny<List<int>>(), It.IsAny<int>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([]);

        _configuration.SetupGet(c => c["ShopBank:BankCode"]).Returns("VCB");
        _configuration.SetupGet(c => c["ShopBank:BankAccount"]).Returns("0123456789");
        _configuration.SetupGet(c => c["ShopBank:BankName"]).Returns("Vietcombank");

        // Act
        var result = await _checkOutService.PlaceOrderAsync(request, fakeUserId, true, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        Assert.Equal("John", result.Value.FullName);
        Assert.Equal("Ha Noi", result.Value.ShippingAddress);
        Assert.Equal("0909", result.Value.PhoneNumber);
        Assert.Equal(InvoiceStatus.Pending, result.Value.Status);
        Assert.Equal(1, variant.Stock);
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
