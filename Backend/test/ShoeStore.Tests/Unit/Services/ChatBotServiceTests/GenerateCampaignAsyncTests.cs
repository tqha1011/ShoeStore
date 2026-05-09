using ErrorOr;
using Microsoft.Extensions.AI;
using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.ChatCompletion;
using Moq;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.StatisticsInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Enum;
using ChatMessage = ShoeStore.Domain.Entities.ChatMessage;

namespace ShoeStore.Tests.Unit.Services.ChatBotServiceTests;

public class GenerateCampaignAsyncTests
{
    private readonly Mock<IChatCompletionService> _chatCompletionService = new();
    private readonly Mock<IChatMessageRepository> _chatMessageRepository = new();
    private readonly Mock<IChatSessionRepository> _chatSessionRepository = new();
    private readonly Mock<IEmbeddingGenerator<string, Embedding<float>>> _embeddingGenerator = new();
    private readonly ChatBotService _service;
    private readonly Mock<IStatisticsService> _statisticsService = new();
    private readonly Mock<IUnitOfWork> _unitOfWork = new();
    private readonly Mock<IProductEmbeddingRepository> _productEmbeddingRepository = new();

    public GenerateCampaignAsyncTests()
    {
        _service = new ChatBotService(
            _statisticsService.Object,
            _chatCompletionService.Object,
            _chatMessageRepository.Object,
            _chatSessionRepository.Object,
            _unitOfWork.Object,
            _embeddingGenerator.Object,
            _productEmbeddingRepository.Object);
    }

    [Fact]
    public async Task GenerateCampaignAsync_WhenSummaryDataFails_ReturnsError()
    {
        // Arrange
        var request = new CreateCampaignRequestDto(Guid.NewGuid(), "Generate campaign");
        var error = Error.Unexpected("Summary.Failed", "Summary failed");
        _statisticsService.Setup(s => s.GetStatisticsSummaryAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(error);

        // Act
        var result = await _service.GenerateCampaignAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Summary.Failed", result.FirstError.Code);
        _chatMessageRepository.Verify(r => r.Add(It.IsAny<ChatMessage>()), Times.Never);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task GenerateCampaignAsync_WhenTopProductsFail_ReturnsError()
    {
        // Arrange
        var request = new CreateCampaignRequestDto(Guid.NewGuid(), "Generate campaign");
        var summary = BuildSummary();
        var error = Error.Unexpected("TopProducts.Failed", "Top products failed");
        _statisticsService.Setup(s => s.GetStatisticsSummaryAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(summary);
        _statisticsService.Setup(s => s.GetProductsHighestStatisticsAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(error);

        // Act
        var result = await _service.GenerateCampaignAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("TopProducts.Failed", result.FirstError.Code);
        _chatMessageRepository.Verify(r => r.Add(It.IsAny<ChatMessage>()), Times.Never);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task GenerateCampaignAsync_WhenSessionNotFound_ReturnsNotFound()
    {
        // Arrange
        var request = new CreateCampaignRequestDto(Guid.NewGuid(), "Generate campaign");
        var summary = BuildSummary();
        var topProducts = BuildTopProducts();

        _statisticsService.Setup(s => s.GetStatisticsSummaryAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(summary);
        _statisticsService.Setup(s => s.GetProductsHighestStatisticsAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(topProducts);
        _chatSessionRepository
            .Setup(r => r.GetChatSessionIdByPublicIdAsync(request.PublicSessionId, It.IsAny<CancellationToken>()))
            .ReturnsAsync((int?)null);

        // Act
        var result = await _service.GenerateCampaignAsync(request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("ChatSession.NotFound", result.FirstError.Code);
        _chatMessageRepository.Verify(r => r.Add(It.IsAny<ChatMessage>()), Times.Never);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task GenerateCampaignAsync_WhenValidRequest_ReturnsStreamAndPersistsUserMessage()
    {
        // Arrange
        var request = new CreateCampaignRequestDto(Guid.NewGuid(), "Generate campaign");
        var summary = BuildSummary();
        var topProducts = BuildTopProducts();
        const int sessionId = 10;

        _statisticsService.Setup(s => s.GetStatisticsSummaryAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(summary);
        _statisticsService.Setup(s => s.GetProductsHighestStatisticsAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(topProducts);
        _chatSessionRepository
            .Setup(r => r.GetChatSessionIdByPublicIdAsync(request.PublicSessionId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(sessionId);
        _chatCompletionService
            .Setup(s => s.GetStreamingChatMessageContentsAsync(
                It.IsAny<ChatHistory>(),
                It.IsAny<PromptExecutionSettings>(),
                It.IsAny<Kernel?>(),
                It.IsAny<CancellationToken>()))
            .Returns(BuildStreamingResponse("Buy ", "1 ", "Get 1"));

        // Act
        var result = await _service.GenerateCampaignAsync(request, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);

        // Read only streaming chunks (avoid completing the iterator to keep SaveChangesAsync at one call).
        var output = string.Empty;
        await foreach (var chunk in result.Value)
        {
            output += chunk;
            if (output == "Buy 1 Get 1") break;
        }

        Assert.Equal("Buy 1 Get 1", output);
        _chatMessageRepository.Verify(r => r.Add(It.Is<ChatMessage>(m =>
            m.Role == ChatBotRole.User && m.Content == request.Content && m.SessionId == sessionId)), Times.Once);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Exactly(2));
    }

    private static StatisticsSummaryResponseDto BuildSummary()
    {
        return new StatisticsSummaryResponseDto(
            1000000m,
            10,
            100000m,
            10m,
            5m,
            20m);
    }

    private static List<ProductHighestStatisticsResponseDto> BuildTopProducts()
    {
        return
        [
            new ProductHighestStatisticsResponseDto(
                Guid.NewGuid(),
                "Air Jordan",
                "img",
                10,
                1000000m,
                50m)
        ];
    }

    private static async IAsyncEnumerable<StreamingChatMessageContent> BuildStreamingResponse(
        params string[] chunks)
    {
        foreach (var chunk in chunks)
        {
            yield return new StreamingChatMessageContent(AuthorRole.Assistant, chunk);
            await Task.Yield();
        }
    }
}