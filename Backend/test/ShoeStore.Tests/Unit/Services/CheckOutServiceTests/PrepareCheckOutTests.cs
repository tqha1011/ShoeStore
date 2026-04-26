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

namespace ShoeStore.Tests.Unit.Services.CheckOutServiceTests;

public class PrepareCheckOutTests
{
    private readonly Mock<ICartItemRepository> _cartItemRepository = new();
    private readonly CheckOutService _checkOutService;

    private readonly Mock<IInvoiceRepository> _invoiceRepository = new();

    private readonly Mock<IUnitOfWork> _mockUow = new();

    private readonly Mock<IProductVariantRepository> _productVariantRepository = new();

    private readonly Mock<IUserRepository> _userRepository = new();

    public PrepareCheckOutTests()
    {
        _checkOutService = new CheckOutService(_productVariantRepository.Object, _mockUow.Object,
            _cartItemRepository.Object, _invoiceRepository.Object, _userRepository.Object);
    }

    [Fact]
    public async Task PrepareCheckOut_WhenVariantDoesNotExist_ReturnNotFound()
    {
        var fakeVariantId = Guid.NewGuid();
        List<CheckOutRequestDto> fakeRequest =
        [
            new(fakeVariantId)
        ];

        List<ProductVariant> fakeProductVariants = [];

        _productVariantRepository
            .Setup(x => x.GetListVariantsAsync(It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeProductVariants);

        var result = await _checkOutService.PrepareCheckOutAsync(fakeRequest, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("Variant.NotFound", result.FirstError.Code);
    }

    [Fact]
    public async Task PrepareCheckOut_WhenVariantOutOfStock_ReturnIsOutOfStock()
    {
        var fakeVariantId = Guid.NewGuid();
        List<CheckOutRequestDto> fakeRequest =
        [
            new(fakeVariantId)
        ];

        var fakeProduct = BuildFakeProduct();

        var fakeColor = new Color
        {
            Id = 1,
            ColorName = "Color Name"
        };

        var fakeSize = new ProductSize
        {
            Id = 1,
            Size = 40
        };


        var fakeProductVariants = BuildFakeProductVariants(fakeProduct, fakeColor, fakeSize, fakeVariantId, true);

        _productVariantRepository
            .Setup(x => x.GetListVariantsAsync(It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeProductVariants);

        var result = await _checkOutService.PrepareCheckOutAsync(fakeRequest, CancellationToken.None);

        Assert.False(result.IsError);
        var item = Assert.Single(result.Value.Items);
        Assert.True(item.IsOutOfStock);
    }

    // test case for happy path
    [Fact]
    public async Task PrepareCheckOut_WhenVariantsExist_ReturnCheckOutResponseDto()
    {
        var fakeVariantId = Guid.NewGuid();
        List<CheckOutRequestDto> fakeRequest =
        [
            new(fakeVariantId)
        ];

        var fakeProduct = BuildFakeProduct();

        var fakeColor = new Color
        {
            Id = 1,
            ColorName = "Color Name"
        };

        var fakeSize = new ProductSize
        {
            Id = 1,
            Size = 40
        };

        var fakeProductVariants = BuildFakeProductVariants(fakeProduct, fakeColor, fakeSize, fakeVariantId);

        _productVariantRepository
            .Setup(x => x.GetListVariantsAsync(It.IsAny<List<Guid>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(fakeProductVariants);

        var result = await _checkOutService.PrepareCheckOutAsync(fakeRequest, CancellationToken.None);

        Assert.False(result.IsError);

        Assert.Equal(100, result.Value.Summary.TotalPrice);
        Assert.Equal(100, result.Value.Summary.FinalPrice);

        var item = Assert.Single(result.Value.Items);
        Assert.Equal(fakeVariantId, item.VariantId);
        Assert.Equal("Product Name", item.ProductName);
        Assert.Equal("Color Name", item.ColorName);
        Assert.Equal(40, item.Size);
        Assert.Equal(100, item.UnitPrice);
        Assert.Equal(1, item.Quantity);
        Assert.Equal(20, item.StockAvailable);
        Assert.False(item.IsOutOfStock);
        Assert.Equal(100, item.SubTotal);
    }

    private static Product BuildFakeProduct()
    {
        return new Product
        {
            Id = 1,
            ProductName = "Product Name",
            CategoryId = 1,
            Category = new Category
            {
                Id = 1,
                Name = "Category Name"
            }
        };
    }

    private static List<ProductVariant> BuildFakeProductVariants(Product fakeProduct, Color fakeColor,
        ProductSize fakeSize, Guid fakeVariantId, bool isOutOfStock = false)
    {
        List<ProductVariant> fakeProductVariants =
        [
            new()
            {
                Id = 1,
                ProductId = 1,
                Product = fakeProduct,
                ColorId = 1,
                Color = fakeColor,
                Price = 100,
                PublicId = fakeVariantId,
                SizeId = 1,
                Size = fakeSize,
                Stock = isOutOfStock ? 0 : 20,
                IsSelling = true
            }
        ];
        return fakeProductVariants;
    }
}