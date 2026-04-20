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
}