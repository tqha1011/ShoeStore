using ErrorOr;
using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.ChatCompletion;
using Moq;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.StatisticsInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Tests.Unit.Services.ChatBotServiceTests;

public class ChatAskAboutStatisticsAsyncTests
{
    private readonly Mock<IChatCompletionService> _chatCompletionService = new();
    private readonly Mock<IChatMessageRepository> _chatMessageRepository = new();
    private readonly Mock<IChatSessionRepository> _chatSessionRepository = new();
    private readonly ChatBotService _service;
    private readonly Mock<IStatisticsService> _statisticsService = new();
    private readonly Mock<IUnitOfWork> _unitOfWork = new();

    public ChatAskAboutStatisticsAsyncTests()
    {
        _service = new ChatBotService(
            _statisticsService.Object,
            _chatCompletionService.Object,
            _chatMessageRepository.Object,
            _chatSessionRepository.Object,
            _unitOfWork.Object);
    }

    [Fact]
    public async Task ChatAskAboutStatisticsAsync_WhenSummaryDataFails_ReturnsError()
    {
        // Arrange
        var request = new ChatMessageRequestDto("How is revenue?");
        var error = Error.Unexpected("Summary.Failed", "Summary failed");
        _statisticsService.Setup(s => s.GetStatisticsSummaryAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(error);

        // Act
        var result = await _service.ChatAskAboutStatisticsAsync(Guid.NewGuid(), request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("Summary.Failed", result.FirstError.Code);
        _chatMessageRepository.Verify(r => r.Add(It.IsAny<ChatMessage>()), Times.Never);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task ChatAskAboutStatisticsAsync_WhenTopProductsFail_ReturnsError()
    {
        // Arrange
        var request = new ChatMessageRequestDto("How is revenue?");
        var summary = BuildSummary();
        var error = Error.Unexpected("TopProducts.Failed", "Top products failed");
        _statisticsService.Setup(s => s.GetStatisticsSummaryAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(summary);
        _statisticsService.Setup(s => s.GetProductsHighestStatisticsAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(error);

        // Act
        var result = await _service.ChatAskAboutStatisticsAsync(Guid.NewGuid(), request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("TopProducts.Failed", result.FirstError.Code);
        _chatMessageRepository.Verify(r => r.Add(It.IsAny<ChatMessage>()), Times.Never);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task ChatAskAboutStatisticsAsync_WhenSessionNotFound_ReturnsNotFound()
    {
        // Arrange
        var request = new ChatMessageRequestDto("How is revenue?");
        var summary = BuildSummary();
        var topProducts = BuildTopProducts();

        _statisticsService.Setup(s => s.GetStatisticsSummaryAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(summary);
        _statisticsService.Setup(s => s.GetProductsHighestStatisticsAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(topProducts);
        _chatSessionRepository
            .Setup(r => r.GetChatSessionIdByPublicIdAsync(It.IsAny<Guid>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync((int?)null);

        // Act
        var result = await _service.ChatAskAboutStatisticsAsync(Guid.NewGuid(), request, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("ChatSession.NotFound", result.FirstError.Code);
        _chatMessageRepository.Verify(r => r.Add(It.IsAny<ChatMessage>()), Times.Never);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task ChatAskAboutStatisticsAsync_WhenValidRequest_ReturnsStreamAndPersistsUserMessage()
    {
        // Arrange
        var request = new ChatMessageRequestDto("Show me the stats");
        var summary = BuildSummary();
        var topProducts = BuildTopProducts();
        var sessionPublicId = Guid.NewGuid();
        const int sessionId = 22;

        var history = new List<ChatMessage>
        {
            new()
            {
                Content = "Previous user message",
                Role = ChatBotRole.User,
                SessionId = sessionId,
                CreatedAt = DateTime.UtcNow.AddMinutes(-2),
                TokenCount = 2
            },
            new()
            {
                Content = "Previous assistant response",
                Role = ChatBotRole.Assistant,
                SessionId = sessionId,
                CreatedAt = DateTime.UtcNow.AddMinutes(-1),
                TokenCount = 2
            }
        };

        _statisticsService.Setup(s => s.GetStatisticsSummaryAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(summary);
        _statisticsService.Setup(s => s.GetProductsHighestStatisticsAsync(It.IsAny<CancellationToken>()))
            .ReturnsAsync(topProducts);
        _chatSessionRepository
            .Setup(r => r.GetChatSessionIdByPublicIdAsync(sessionPublicId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(sessionId);
        _chatMessageRepository
            .Setup(r => r.GetHistoryChatMessageAsync(sessionId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(history);
        _chatCompletionService
            .Setup(s => s.GetStreamingChatMessageContentsAsync(
                It.IsAny<ChatHistory>(),
                It.IsAny<PromptExecutionSettings>(),
                It.IsAny<Kernel?>(),
                It.IsAny<CancellationToken>()))
            .Returns(BuildStreamingResponse("Revenue ", "is ", "up"));

        // Act
        var result = await _service.ChatAskAboutStatisticsAsync(sessionPublicId, request, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);

        // Read only streaming chunks (avoid completing the iterator to keep SaveChangesAsync at one call).
        var output = string.Empty;
        await foreach (var chunk in result.Value)
        {
            output += chunk;
            if (output == "Revenue is up") break;
        }

        Assert.Equal("Revenue is up", output);
        _chatMessageRepository.Verify(r => r.Add(It.Is<ChatMessage>(m =>
            m.Role == ChatBotRole.User && m.Content == request.Content && m.SessionId == sessionId)), Times.Once);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    private static StatisticsSummaryResponseDto BuildSummary()
    {
        return new StatisticsSummaryResponseDto(
            1500000m,
            12,
            125000m,
            12m,
            6m,
            25m);
    }

    private static List<ProductHighestStatisticsResponseDto> BuildTopProducts()
    {
        return
        [
            new ProductHighestStatisticsResponseDto(
                Guid.NewGuid(),
                "Yeezy",
                "img",
                8,
                750000m,
                30m)
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