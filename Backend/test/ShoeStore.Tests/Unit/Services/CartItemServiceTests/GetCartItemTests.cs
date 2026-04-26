using MockQueryable;
using Moq;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Tests.Unit.Services.CartItemServiceTests;

public class GetCartItemTests
{
    private readonly Mock<ICartItemRepository> _cartItemRepository = new();
    private readonly CartItemService _cartItemService;
    private readonly Mock<IUnitOfWork> _mockUow = new();
    private readonly Mock<IUserRepository> _userRepository = new();
    private readonly Mock<IProductVariantRepository> _variantRepository = new();

    public GetCartItemTests()
    {
        _cartItemService = new CartItemService(_cartItemRepository.Object, _mockUow.Object, _variantRepository.Object,
            _userRepository.Object);
    }

    [Fact]
    public async Task GetCartItemsByUserIdAsync_WhenUserNotFound_ReturnsNotFound()
    {
        // Arrange
        var userPublicId = Guid.NewGuid();
        _userRepository.Setup(x => x.CheckUserExistsAsync(userPublicId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(false);

        // Act
        var result = await _cartItemService.GetCartItemsByUserIdAsync(userPublicId, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("User.NotFound", result.FirstError.Code);
        _cartItemRepository.Verify(x => x.GetCartItemsByUserId(It.IsAny<Guid>()), Times.Never);
    }

    [Fact]
    public async Task GetCartItemsByUserIdAsync_WhenCartItemsExist_ReturnsMappedResponse()
    {
        // Arrange
        var userPublicId = Guid.NewGuid();
        var cartItemPublicId = Guid.NewGuid();
        var variantPublicId = Guid.NewGuid();

        var fakeCartItems = new List<CartItem>
        {
            new()
            {
                PublicId = cartItemPublicId,
                UserId = 1,
                Quantity = 2,
                ProductVariantId = 1,
                ProductVariant = CreateProductVariant(variantPublicId)
            }
        }.BuildMock().AsQueryable();

        _userRepository.Setup(x => x.CheckUserExistsAsync(userPublicId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(true);
        _cartItemRepository.Setup(x => x.GetCartItemsByUserId(userPublicId))
            .Returns(fakeCartItems);

        // Act
        var result = await _cartItemService.GetCartItemsByUserIdAsync(userPublicId, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        var cartItems = result.Value;
        Assert.Single(cartItems);

        var cartItem = cartItems[0];
        Assert.Equal(cartItemPublicId, cartItem.CartItemId);
        Assert.Equal(2, cartItem.Quantity);
        Assert.Equal(variantPublicId, cartItem.ProductVariantId);
        Assert.Equal(1, cartItem.ColorId);
        Assert.Equal("Black", cartItem.ColorName);
        Assert.Equal(42, cartItem.Size);
        Assert.Equal(100, cartItem.Price);
        Assert.Equal("Jordan 1", cartItem.ProductName);
        Assert.Equal("Nike", cartItem.Brand);
        Assert.Equal("https://image.test/shoe.jpg", cartItem.ImageUrl);
        Assert.Equal(5, cartItem.Stock);
        Assert.True(cartItem.IsSelling);
    }

    private static ProductVariant CreateProductVariant(Guid publicId)
    {
        return new ProductVariant
        {
            Id = 1,
            PublicId = publicId,
            SizeId = 1,
            ProductId = 1,
            ColorId = 1,
            Stock = 5,
            IsSelling = true,
            ImageUrl = "https://image.test/shoe.jpg",
            Price = 100,
            Product = new Product
            {
                Id = 1,
                ProductName = "Jordan 1",
                Brand = "Nike"
            },
            Color = new Color
            {
                Id = 1,
                ColorName = "Black"
            },
            Size = new ProductSize
            {
                Id = 1,
                Size = 42
            }
        };
    }
}