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
        var request = CreateUpdateCartItemDto(Guid.NewGuid(), Guid.NewGuid(), 2);
        _cartItemRepository
            .Setup(x => x.GetCartItemByGuidAsync(request.CartItemId, It.IsAny<CancellationToken>(), true))
            .ReturnsAsync((CartItem?)null);

        // Act
        var result = await _cartItemService.UpdateCartItemAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("CartItem.NotFound", result.FirstError.Code);
        VerifyWriteActions(Times.Never);
    }

    [Fact]
    public async Task UpdateCartItemAsync_WhenProductVariantNotFound_ReturnsNotFound()
    {
        // Arrange
        var cartItemId = Guid.NewGuid();
        var variantPublicId = Guid.NewGuid();
        var request = CreateUpdateCartItemDto(cartItemId, variantPublicId, 2);
        var cartItem = CreateCartItem(cartItemId, productVariantId: 1, quantity: 1);

        _cartItemRepository
            .Setup(x => x.GetCartItemByGuidAsync(cartItemId, It.IsAny<CancellationToken>(), true))
            .ReturnsAsync(cartItem);
        _variantRepository
            .Setup(x => x.GetByGuidAsync(variantPublicId, It.IsAny<CancellationToken>()))
            .ReturnsAsync((ProductVariant?)null);

        // Act
        var result = await _cartItemService.UpdateCartItemAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("ProductVariant.NotFound", result.FirstError.Code);
        VerifyWriteActions(Times.Never);
    }

    [Fact]
    public async Task UpdateCartItemAsync_WhenVariantChangedAndExistingItemExceedsStock_ReturnsValidationError()
    {
        // Arrange
        var cartItemId = Guid.NewGuid();
        var variantPublicId = Guid.NewGuid();
        var request = CreateUpdateCartItemDto(cartItemId, variantPublicId, 2);

        // Current cart item is pointing to another variant, so service checks existing item by target variant id.
        var currentCartItem = CreateCartItem(cartItemId, productVariantId: 1, quantity: 1);
        var targetVariant = CreateProductVariant(variantPublicId, id: 2, stock: 3);
        var existingItem = CreateCartItem(Guid.NewGuid(), productVariantId: 2, quantity: 2);

        _cartItemRepository
            .Setup(x => x.GetCartItemByGuidAsync(cartItemId, It.IsAny<CancellationToken>(), true))
            .ReturnsAsync(currentCartItem);
        _variantRepository
            .Setup(x => x.GetByGuidAsync(variantPublicId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(targetVariant);
        _cartItemRepository
            .Setup(x => x.GetExistCartItemAsync(currentCartItem.UserId, targetVariant.Id, It.IsAny<CancellationToken>()))
            .ReturnsAsync(existingItem);

        // Act
        var result = await _cartItemService.UpdateCartItemAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("CartItem.QuantityExceedsStock", result.FirstError.Code);
        _cartItemRepository.Verify(x => x.Update(It.IsAny<CartItem>()), Times.Never);
        _cartItemRepository.Verify(x => x.Delete(It.IsAny<CartItem>()), Times.Never);
        _mockUow.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task UpdateCartItemAsync_WhenVariantChangedAndExistingItemFound_UpdatesAndDeletesOldItem()
    {
        // Arrange
        var cartItemId = Guid.NewGuid();
        var variantPublicId = Guid.NewGuid();
        var request = CreateUpdateCartItemDto(cartItemId, variantPublicId, 2);

        var currentCartItem = CreateCartItem(cartItemId, productVariantId: 1, quantity: 1);
        var targetVariant = CreateProductVariant(variantPublicId, id: 2, stock: 10);
        var existingItem = CreateCartItem(Guid.NewGuid(), productVariantId: 2, quantity: 3);

        _cartItemRepository
            .Setup(x => x.GetCartItemByGuidAsync(cartItemId, It.IsAny<CancellationToken>(), true))
            .ReturnsAsync(currentCartItem);
        _variantRepository
            .Setup(x => x.GetByGuidAsync(variantPublicId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(targetVariant);
        _cartItemRepository
            .Setup(x => x.GetExistCartItemAsync(currentCartItem.UserId, targetVariant.Id, It.IsAny<CancellationToken>()))
            .ReturnsAsync(existingItem);

        // Act
        var result = await _cartItemService.UpdateCartItemAsync(request, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        Assert.Equal(5, existingItem.Quantity); // 3 + request quantity 2
        Assert.Equal(existingItem.PublicId, result.Value.CartItemId);
        Assert.Equal(existingItem.Quantity, result.Value.Quantity);
        Assert.Equal(targetVariant.PublicId, result.Value.ProductVariantId);

        _cartItemRepository.Verify(x => x.Update(existingItem), Times.Once);
        _cartItemRepository.Verify(x => x.Delete(currentCartItem), Times.Once);
        _mockUow.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task UpdateCartItemAsync_WhenSameVariantAndQuantityExceedsStock_ReturnsValidationError()
    {
        // Arrange
        var cartItemId = Guid.NewGuid();
        var variantPublicId = Guid.NewGuid();
        var request = CreateUpdateCartItemDto(cartItemId, variantPublicId, 9);

        var cartItem = CreateCartItem(cartItemId, productVariantId: 1, quantity: 1);
        var variant = CreateProductVariant(variantPublicId, id: 1, stock: 5);

        _cartItemRepository
            .Setup(x => x.GetCartItemByGuidAsync(cartItemId, It.IsAny<CancellationToken>(), true))
            .ReturnsAsync(cartItem);
        _variantRepository
            .Setup(x => x.GetByGuidAsync(variantPublicId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(variant);

        // Act
        var result = await _cartItemService.UpdateCartItemAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("CartItem.QuantityExceedsStock", result.FirstError.Code);
        _mockUow.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task UpdateCartItemAsync_WhenSameVariantAndQuantityIsValid_ReturnsUpdatedCartItem()
    {
        // Arrange
        var cartItemId = Guid.NewGuid();
        var variantPublicId = Guid.NewGuid();
        var request = CreateUpdateCartItemDto(cartItemId, variantPublicId, 4);

        var cartItem = CreateCartItem(cartItemId, productVariantId: 1, quantity: 1);
        var variant = CreateProductVariant(variantPublicId, id: 1, stock: 5);

        _cartItemRepository
            .Setup(x => x.GetCartItemByGuidAsync(cartItemId, It.IsAny<CancellationToken>(), true))
            .ReturnsAsync(cartItem);
        _variantRepository
            .Setup(x => x.GetByGuidAsync(variantPublicId, It.IsAny<CancellationToken>()))
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
        _cartItemRepository.Verify(x => x.Update(It.IsAny<CartItem>()), Times.Never);
        _cartItemRepository.Verify(x => x.Delete(It.IsAny<CartItem>()), Times.Never);
    }

    private void VerifyWriteActions(Func<Times> times)
    {
        _cartItemRepository.Verify(x => x.Update(It.IsAny<CartItem>()), times);
        _cartItemRepository.Verify(x => x.Delete(It.IsAny<CartItem>()), times);
        _mockUow.Verify(x => x.SaveChangesAsync(It.IsAny<CancellationToken>()), times);
    }

    private static UpdateCartItemDto CreateUpdateCartItemDto(Guid cartItemId, Guid variantPublicId, int quantity)
    {
        return new UpdateCartItemDto
        {
            CartItemId = cartItemId,
            NewProductVariantId = variantPublicId,
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