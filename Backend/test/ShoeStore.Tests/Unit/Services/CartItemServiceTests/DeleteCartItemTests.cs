using ErrorOr;
using Moq;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Tests.Unit.Services.CartItemServiceTests;

public class DeleteCartItemTests
{
    private readonly Mock<ICartItemRepository> _cartItemRepository = new();
    private readonly CartItemService _cartItemService;
    private readonly Mock<IUnitOfWork> _mockUow = new();
    private readonly Mock<IUserRepository> _userRepository = new();
    private readonly Mock<IProductVariantRepository> _variantRepository = new();

    public DeleteCartItemTests()
    {
        _cartItemService = new CartItemService(_cartItemRepository.Object, _mockUow.Object, _variantRepository.Object,
            _userRepository.Object);
    }

    [Fact]
    public async Task DeleteCartItemAsync_WhenCartItemNotFound_ReturnsNotFound()
    {
        var fakeCartItemIds = new List<Guid> { Guid.NewGuid(), Guid.NewGuid() };
        var fakeUserId = Guid.NewGuid();
        _cartItemRepository.Setup(cart =>
                cart.GetListOfCartItemsAsync(It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([]);

        var result = await _cartItemService.DeleteCartItemAsync(fakeCartItemIds, fakeUserId, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("CartItem.NotFound", result.FirstError.Code);
        _mockUow.Verify(uow => uow.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task DeleteCartItemAsync_WhenUserNotAuthorized_ReturnsUnauthorized()
    {
        var fakeUserId = Guid.NewGuid();
        var fakeCartId = Guid.NewGuid();
        var fakeCartItem = CreateCartItem(fakeCartId);
        var fakeCartItemIds = new List<Guid> { fakeCartId };
        _cartItemRepository.Setup(cart =>
                cart.GetListOfCartItemsAsync(fakeCartItemIds, It.IsAny<CancellationToken>()))
            .ReturnsAsync([fakeCartItem]);

        var result = await _cartItemService.DeleteCartItemAsync(fakeCartItemIds, fakeUserId, CancellationToken.None);
        Assert.True(result.IsError);
        Assert.Equal("User.Unauthorized", result.FirstError.Code);
        _mockUow.Verify(uow => uow.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    // test happy path
    [Fact]
    public async Task DeleteCartItemAsync_WhenCartItemDeleted_ReturnsSuccess()
    {
        var fakeUserId = Guid.NewGuid();
        var fakeCartId = Guid.NewGuid();
        var fakeUser = CreateUser(fakeUserId);
        var fakeCartItem = CreateCartItem(fakeCartId);
        fakeCartItem.User =  fakeUser;
        var fakeCartItemIds = new List<Guid> { fakeCartId };
        _cartItemRepository.Setup(cart =>
                cart.GetListOfCartItemsAsync(fakeCartItemIds, It.IsAny<CancellationToken>()))
            .ReturnsAsync([fakeCartItem]);

        var result = await _cartItemService.DeleteCartItemAsync(fakeCartItemIds, fakeUserId, CancellationToken.None);
        Assert.False(result.IsError);
        _mockUow.Verify(uow => uow.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    private static CartItem CreateCartItem(Guid fakeCartItemId)
    {
        return new CartItem
        {
            Id = 1,
            ProductVariantId = 1,
            PublicId = fakeCartItemId,
            Quantity = 2,
            UserId = 1
        };
    }

    private static User CreateUser(Guid fakeUserId)
    {
        return new User
        {
            Id = 1,
            Password = "password",
            Email = "email",
            PublicId = fakeUserId,
            UserName = ""
        };
    }
}