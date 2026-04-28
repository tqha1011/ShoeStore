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

public class UpdateCartItemTests
{
    private readonly Mock<ICartItemRepository> _cartItemRepository = new();
    private readonly CartItemService _cartItemService;
    private readonly Mock<IUnitOfWork> _mockUow = new();
    private readonly Mock<IProductVariantRepository> _variantRepository = new();
    private readonly Mock<IUserRepository> _userRepository = new();

    public UpdateCartItemTests()
    {
        _cartItemService = new CartItemService(_cartItemRepository.Object, _mockUow.Object, _variantRepository.Object,
            _userRepository.Object);
    }

    [Fact]
    public async Task UpdateCartItemAsync_WhenCartItemNotFound_ReturnsNotFound()
    {
        // Arrange
        var request = CreateUpdateCartItemDto(Guid.NewGuid(), 2);
        _cartItemRepository
            .Setup(x => x.GetCartItemByGuidAsync(request.CartItemId, It.IsAny<CancellationToken>(), true))
            .ReturnsAsync((CartItem?)null);

        // Act
        var result = await _cartItemService.UpdateCartItemAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("CartItem.NotFound", result.FirstError.Code);
        _mockUow.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task UpdateCartItemAsync_WhenProductVariantNotFound_ReturnsNotFound()
    {
        // Arrange
        var cartItemId = Guid.NewGuid();
        var request = CreateUpdateCartItemDto(cartItemId, 2);
        var cartItem = CreateCartItem(cartItemId, productVariantId: 1, quantity: 1);

        _cartItemRepository
            .Setup(x => x.GetCartItemByGuidAsync(cartItemId, It.IsAny<CancellationToken>(), true))
            .ReturnsAsync(cartItem);
        _variantRepository
            .Setup(x => x.GetByIdWithIncludesAsync(cartItem.ProductVariantId, It.IsAny<CancellationToken>()))
            .ReturnsAsync((ProductVariant?)null);

        // Act
        var result = await _cartItemService.UpdateCartItemAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("ProductVariant.NotFound", result.FirstError.Code);
        _mockUow.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task UpdateCartItemAsync_WhenQuantityExceedsStock_ReturnsValidationError()
    {
        // Arrange
        var cartItemId = Guid.NewGuid();
        var request = CreateUpdateCartItemDto(cartItemId, 9);

        var cartItem = CreateCartItem(cartItemId, productVariantId: 1, quantity: 1);
        var variant = CreateProductVariant(Guid.NewGuid(), id: 1, stock: 5);

        _cartItemRepository
            .Setup(x => x.GetCartItemByGuidAsync(cartItemId, It.IsAny<CancellationToken>(), true))
            .ReturnsAsync(cartItem);
        _variantRepository
            .Setup(x => x.GetByIdWithIncludesAsync(cartItem.ProductVariantId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(variant);

        // Act
        var result = await _cartItemService.UpdateCartItemAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("CartItem.QuantityExceedsStock", result.FirstError.Code);
        _mockUow.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task UpdateCartItemAsync_WhenQuantityIsValid_ReturnsUpdatedCartItem()
    {
        // Arrange
        var cartItemId = Guid.NewGuid();
        var request = CreateUpdateCartItemDto(cartItemId, 4);

        var cartItem = CreateCartItem(cartItemId, productVariantId: 1, quantity: 1);
        var variant = CreateProductVariant(Guid.NewGuid(), id: 1, stock: 5);

        _cartItemRepository
            .Setup(x => x.GetCartItemByGuidAsync(cartItemId, It.IsAny<CancellationToken>(), true))
            .ReturnsAsync(cartItem);
        _variantRepository
            .Setup(x => x.GetByIdWithIncludesAsync(cartItem.ProductVariantId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(variant);

        // Act
        var result = await _cartItemService.UpdateCartItemAsync(request, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        Assert.Equal(request.Quantity, cartItem.Quantity);
        Assert.Equal(cartItem.PublicId, result.Value.CartItemId);
        Assert.Equal(request.Quantity, result.Value.Quantity);
        Assert.Equal(variant.PublicId, result.Value.ProductVariantId);
        Assert.Equal(variant.ColorId, result.Value.ColorId);
        Assert.Equal(variant.SizeId, result.Value.SizeId);
        Assert.Equal(variant.Price, result.Value.Price);
        Assert.Equal(variant.Stock, result.Value.Stock);

        _mockUow.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    private static UpdateCartItemDto CreateUpdateCartItemDto(Guid cartItemId, int quantity)
    {
        return new UpdateCartItemDto
        {
            CartItemId = cartItemId,
            Quantity = quantity
        };
    }

    private static ProductVariant CreateProductVariant(Guid fakePublicId, int id = 1, int stock = 5)
    {
        return new ProductVariant
        {
            Id = id,
            SizeId = 1,
            ProductId = 1,
            ColorId = 1,
            Stock = stock,
            IsSelling = false,
            Price = 100,
            PublicId = fakePublicId
        };
    }

    private static CartItem CreateCartItem(Guid cartItemPublicId, int productVariantId = 1, int userId = 1, int quantity = 2)
    {
        return new CartItem
        {
            UserId = userId,
            Quantity = quantity,
            ProductVariantId = productVariantId,
            PublicId = cartItemPublicId
        };
    }
}