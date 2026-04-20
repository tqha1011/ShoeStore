using MockQueryable;
using Moq;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Tests.Unit.Services.InvoiceServiceTests;

public class GetInvoiceDetailsTests
{
    private readonly Mock<ICurrentUser> _currentUser = new();

    private readonly InvoiceService _getInvoiceDetails;

    // generate mock data by using Moq nuget
    private readonly Mock<IInvoiceRepository> _mockRepo = new();
    private readonly Mock<IUnitOfWork> _mockUow = new();

    public GetInvoiceDetailsTests()
    {
        _getInvoiceDetails = new InvoiceService(_mockRepo.Object, _mockUow.Object, _currentUser.Object);
    }

    [Fact]
    public async Task GetInvoiceDetails_WhenInvoiceDetailsDoesNotExist_ReturnNotFound()
    {
        var fakeGuid = Guid.NewGuid();

        var emptyDetails = new List<InvoiceDetail>().BuildMock().AsQueryable();

        _mockRepo.Setup(repo => repo.GetInvoiceDetail(fakeGuid))
            .Returns(emptyDetails);

        var result = await _getInvoiceDetails.GetInvoiceDetailAsync(fakeGuid, CancellationToken.None);

        Assert.True(result.IsError);
        Assert.Equal("InvoiceDetail.NotFound", result.FirstError.Code);
    }

    [Fact]
    public async Task GetInvoiceDetails_WhenInvoiceDetailsExist_ReturnInvoiceDetailsResponse()
    {
        var fakeGuid = Guid.NewGuid();

        var fakeDetails = new List<InvoiceDetail>
        {
            new()
            {
                Id = 1,
                InvoiceId = 1,
                ProductVariantId = 1,
                Quantity = 2,
                UnitPrice = 120,
                ProductVariant = new ProductVariant
                {
                    Id = 1,
                    ProductId = 1,
                    SizeId = 1,
                    ColorId = 1,
                    Stock = 20,
                    IsSelling = true,
                    Price = 120,
                    ImageUrl = "https://image.test/shoe.jpg",
                    Product = new Product
                    {
                        Id = 1,
                        ProductName = "Jordan 1"
                    },
                    Size = new ProductSize
                    {
                        Id = 1,
                        Size = 42
                    },
                    Color = new Color
                    {
                        Id = 1,
                        ColorName = "Black"
                    }
                }
            }
        }.BuildMock().AsQueryable();

        _mockRepo.Setup(repo => repo.GetInvoiceDetail(fakeGuid))
            .Returns(fakeDetails);

        var result = await _getInvoiceDetails.GetInvoiceDetailAsync(fakeGuid, CancellationToken.None);

        Assert.False(result.IsError);

        var details = result.Value.ToList();
        Assert.Single(details);

        var detail = details[0];
        Assert.Equal("Jordan 1", detail.ProductName);
        Assert.Equal(42, detail.Size);
        Assert.Equal("Black", detail.Color);
        Assert.Equal(2, detail.Quantity);
        Assert.Equal(120, detail.UnitPrice);
        Assert.Equal("https://image.test/shoe.jpg", detail.ImageUrl);
    }
}