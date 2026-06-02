using Moq;
using ShoeStore.Application.DTOs.CartItemDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Tests.Unit.Services.CartItemServiceTests;

public class AddCartItemTests
{
    private readonly Mock<ICartItemRepository> _cartItemRepository = new();
    private readonly CartItemService _cartItemService;
    private readonly Mock<IUnitOfWork> _mockUow = new();
    private readonly Mock<IUserRepository> _userRepository = new();
    private readonly Mock<IProductVariantRepository> _variantRepository = new();

    public AddCartItemTests()
    {
        _cartItemService = new CartItemService(_cartItemRepository.Object, _mockUow.Object, _variantRepository.Object,
            _userRepository.Object);
    }

    [Fact]
    public async Task AddCartItemAsync_WhenVariantNotFound_ReturnsNotFound()
    {
        var fakeRequest = CreateAddCartItemDto(Guid.NewGuid(), 1);
        var fakeUser =  CreateUser(Guid.NewGuid());
        _variantRepository.Setup(x => x.GetByGuidAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync((ProductVariant?)null);
        _userRepository.Setup(x => x.GetUserByPublicIdAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeUser);
        var result =
            await _cartItemService.AddCartItemAsync(fakeRequest, It.IsAny<Guid>(), It.IsAny<CancellationToken>());

        Assert.True(result.IsError);
        Assert.Equal("ProductVariant.NotFound", result.FirstError.Code);
        VerifyDatabaseSafe(Times.Never);
    }

    [Fact]
    public async Task AddCartItemAsync_WhenRequestQuantityOutOfExistCartItemVariantStock_ReturnsValidation()
    {
        var fakeProductVariant = CreateProductVariant(Guid.NewGuid());
        var fakeRequest = CreateAddCartItemDto(Guid.NewGuid(), 4);
        var fakeCartItem = CreateCartItem();
        var fakeUser = CreateUser(Guid.NewGuid());
        _variantRepository.Setup(x => x.GetByGuidAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeProductVariant);
        _cartItemRepository.Setup(cart => cart.GetExistCartItemByGuidAsync(It.IsAny<Guid>(), It.IsAny<Guid>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeCartItem);
        _userRepository.Setup(x => x.GetUserByPublicIdAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeUser);

        var result =
            await _cartItemService.AddCartItemAsync(fakeRequest, It.IsAny<Guid>(), It.IsAny<CancellationToken>());

        Assert.True(result.IsError);
        Assert.Equal("CartItem.QuantityExceedsStock", result.FirstError.Code);
        VerifyDatabaseSafe(Times.Never);
    }

    [Fact]
    public async Task AddCartItemAsync_WhenUserIsNotFound_ReturnsNotFound()
    {
        var fakeRequest = CreateAddCartItemDto(Guid.NewGuid(), 1);
        _userRepository.Setup(x => x.GetUserByPublicIdAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync((User?)null);

        var result = await _cartItemService.AddCartItemAsync(fakeRequest, Guid.NewGuid(), CancellationToken.None);
        Assert.True(result.IsError);
        Assert.Equal("User.NotFound", result.FirstError.Code);
        VerifyDatabaseSafe(Times.Never);
    }

    [Fact]
    public async Task AddCartItemAsync_WhenRequestQuantityOutOfVariantStock_ReturnsValidation()
    {
        var fakeProductVariant = CreateProductVariant(Guid.NewGuid());
        var fakeRequest = CreateAddCartItemDto(Guid.NewGuid(), 6);
        var fakeUserId = Guid.NewGuid();
        var fakeUser = CreateUser(fakeUserId);

        _variantRepository.Setup(x => x.GetByGuidAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeProductVariant);

        _cartItemRepository.Setup(cart => cart.GetExistCartItemByGuidAsync(It.IsAny<Guid>(), It.IsAny<Guid>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync((CartItem?)null);
        _userRepository.Setup(x => x.GetUserByPublicIdAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeUser);

        var result = await _cartItemService.AddCartItemAsync(fakeRequest, fakeUserId, CancellationToken.None);
        Assert.True(result.IsError);
        Assert.Equal("CartItem.QuantityExceedsStock", result.FirstError.Code);
        VerifyDatabaseSafe(Times.Never);
    }
    
    // test case for happy path when cart item exists
    [Fact]
    public async Task AddCartItemAsync_WhenCartItemExists_ReturnsUpdatedCartItem()
    {
        var fakeProductVariant = CreateProductVariant(Guid.NewGuid());
        var fakeRequest = CreateAddCartItemDto(Guid.NewGuid(), 2);
        var fakeUserId = Guid.NewGuid();
        var fakeUser = CreateUser(fakeUserId);
        var fakeCartItem = CreateCartItem();

        _variantRepository.Setup(x => x.GetByGuidAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeProductVariant);

        _cartItemRepository.Setup(cart => cart.GetExistCartItemByGuidAsync(It.IsAny<Guid>(), It.IsAny<Guid>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeCartItem);
        _userRepository.Setup(x => x.GetUserByPublicIdAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeUser);

        var result = await _cartItemService.AddCartItemAsync(fakeRequest, fakeUserId, CancellationToken.None);
        Assert.False(result.IsError);
        Assert.Equal(result.Value.Quantity, fakeCartItem.Quantity);
        Assert.Equal(result.Value.ProductVariantId, fakeProductVariant.PublicId);
        Assert.Equal(result.Value.ColorId, fakeProductVariant.ColorId);
        Assert.Equal(result.Value.SizeId, fakeProductVariant.SizeId);
        Assert.Equal(result.Value.Price, fakeProductVariant.Price);
        Assert.Equal(result.Value.CartItemId, fakeCartItem.PublicId);
        Assert.Equal(result.Value.Stock, fakeProductVariant.Stock);
        Assert.Equal(result.Value.ImageUrl, fakeProductVariant.ImageUrl);
        _mockUow.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task AddCartItemAsync_WhenCartItemDoesNotExist_ReturnsCreatedCartItem()
    {
        // Arrange
        var variantPublicId = Guid.NewGuid();
        var userPublicId = Guid.NewGuid();
        var request = CreateAddCartItemDto(variantPublicId, 2);
        var user = CreateUser(userPublicId);
        var variant = CreateProductVariant(variantPublicId);

        _userRepository
            .Setup(x => x.GetUserByPublicIdAsync(userPublicId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(user);
        _cartItemRepository
            .Setup(x => x.GetExistCartItemByGuidAsync(userPublicId, variantPublicId, It.IsAny<CancellationToken>()))
            .ReturnsAsync((CartItem?)null);
        _variantRepository
            .Setup(x => x.GetByGuidAsync(variantPublicId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(variant);

        CartItem? addedCartItem = null;
        _cartItemRepository
            .Setup(x => x.Add(It.IsAny<CartItem>()))
            .Callback<CartItem>(cartItem => addedCartItem = cartItem);

        // Act
        var result = await _cartItemService.AddCartItemAsync(request, userPublicId, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        Assert.NotNull(addedCartItem);
        Assert.Equal(user.Id, addedCartItem!.UserId);
        Assert.Equal(variant.Id, addedCartItem.ProductVariantId);
        Assert.Equal(request.Quantity, addedCartItem.Quantity);

        Assert.Equal(addedCartItem.PublicId, result.Value.CartItemId);
        Assert.Equal(request.Quantity, result.Value.Quantity);
        Assert.Equal(variant.PublicId, result.Value.ProductVariantId);
        Assert.Equal(variant.ColorId, result.Value.ColorId);
        Assert.Equal(variant.SizeId, result.Value.SizeId);
        Assert.Equal(variant.Price, result.Value.Price);
        Assert.Equal(variant.Stock, result.Value.Stock);
        Assert.Equal(variant.ImageUrl, result.Value.ImageUrl);

        _cartItemRepository.Verify(x => x.Add(It.IsAny<CartItem>()), Times.Once);
        _mockUow.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    private void VerifyDatabaseSafe(Func<Times> times)
    {
        _cartItemRepository.Verify(cart => cart.Add(It.IsAny<CartItem>()), times);
        _mockUow.Verify(uow => uow.SaveChangesAsync(It.IsAny<CancellationToken>()), times);
    }

    private static ProductVariant CreateProductVariant(Guid fakePublicId)
    {
        return new ProductVariant
        {
            Id = 1,
            SizeId = 1,
            ProductId = 1,
            ColorId = 1,
            Stock = 5,
            IsSelling = false,
            Price = 100,
            PublicId = fakePublicId
        };
    }

    private static CartItem CreateCartItem()
    {
        return new CartItem
        {
            UserId = 1,
            Quantity = 2,
            ProductVariantId = 1
        };
    }

    private static AddCartItemDto CreateAddCartItemDto(Guid variantId, int quantity)
    {
        return new AddCartItemDto(variantId, quantity);
    }

    private static User CreateUser(Guid userId)
    {
        return new User
        {
            Id = 1,
            UserName = "",
            Password = "",
            Email = "",
            PublicId = userId
        };
    }
}