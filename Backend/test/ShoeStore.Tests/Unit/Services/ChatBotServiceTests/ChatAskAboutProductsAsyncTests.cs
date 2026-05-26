using Microsoft.Extensions.AI;
using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.ChatCompletion;
using Moq;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.StatisticsInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Entities.Embedding;
using ShoeStore.Domain.Enum;
using ChatMessage = ShoeStore.Domain.Entities.ChatMessage;

namespace ShoeStore.Tests.Unit.Services.ChatBotServiceTests;

public class ChatAskAboutProductsAsyncTests
{
    private readonly Mock<IChatCompletionService> _chatCompletionService = new();
    private readonly Mock<IChatMessageRepository> _chatMessageRepository = new();
    private readonly Mock<IChatSessionRepository> _chatSessionRepository = new();
    private readonly Mock<ICurrentUser> _currentUser = new();
    private readonly Mock<IEmbeddingGenerator<string, Embedding<float>>> _embeddingGenerator = new();
    private readonly Kernel _kernel = Kernel.CreateBuilder().Build();
    private readonly Mock<IMasterDataPluginService> _masterDataPluginService = new();
    private readonly Mock<IProductEmbeddingRepository> _productEmbeddingRepository = new();
    private readonly Mock<IProductPluginService> _productPluginService = new();
    private readonly Mock<IUpdateTitleQueue> _queue = new();
    private readonly ChatBotService _service;
    private readonly Mock<IStatisticsService> _statisticsService = new();
    private readonly Mock<IUnitOfWork> _unitOfWork = new();
    private readonly Mock<IUserRepository> _userRepository = new();

    public ChatAskAboutProductsAsyncTests()
    {
        _service = new ChatBotService(
            _statisticsService.Object,
            _chatCompletionService.Object,
            _chatMessageRepository.Object,
            _chatSessionRepository.Object,
            _unitOfWork.Object,
            _embeddingGenerator.Object,
            _productEmbeddingRepository.Object,
            _userRepository.Object,
            _queue.Object,
            _currentUser.Object,
            _kernel,
            _productPluginService.Object,
            _masterDataPluginService.Object);
    }

    [Fact]
    public async Task ChatAskAboutProductsAsync_WhenSessionNotFound_ReturnsNotFound()
    {
        // Arrange
        var request = new ChatMessageRequestDto("What shoes are trending?");
        var publicUserId = Guid.NewGuid();
        _userRepository
            .Setup(r => r.GetUserIdByPublicIdAsync(publicUserId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(1);
        _chatSessionRepository
            .Setup(r => r.GetChatSessionIdByPublicIdAsync(It.IsAny<Guid>(), It.IsAny<int>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync((int?)null);

        // Act
        var result =
            await _service.ChatAskAboutProductsAsync(Guid.NewGuid(), request, publicUserId, CancellationToken.None);

        // Assert
        Assert.True(result.IsError);
        Assert.Equal("ChatSession.NotFound", result.FirstError.Code);
        _chatMessageRepository.Verify(r => r.Add(It.IsAny<ChatMessage>()), Times.Never);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Never);
    }

    [Fact]
    public async Task ChatAskAboutProductsAsync_WhenValidRequestWithProducts_ReturnsStreamAndPersistsMessages()
    {
        // Arrange
        var request = new ChatMessageRequestDto("Show me popular sneakers");
        var sessionPublicId = Guid.NewGuid();
        var publicUserId = Guid.NewGuid();
        const int sessionId = 12;

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

        _userRepository
            .Setup(r => r.GetUserIdByPublicIdAsync(publicUserId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(1);
        _chatSessionRepository
            .Setup(r => r.GetChatSessionIdByPublicIdAsync(sessionPublicId, It.IsAny<int>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(sessionId);
        _chatMessageRepository
            .Setup(r => r.GetHistoryChatMessageAsync(sessionId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(history);
        _embeddingGenerator
            .Setup(g => g.GenerateAsync(
                It.IsAny<IEnumerable<string>>(),
                It.IsAny<EmbeddingGenerationOptions?>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync([
                new Embedding<float>(new ReadOnlyMemory<float>(new[] { 0.1f, 0.2f }))
            ]);
        _productEmbeddingRepository
            .Setup(r => r.GetTop5ProductByVectorAsync(It.IsAny<ReadOnlyMemory<float>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync(
            [
                new ProductEmbedding
                {
                    ProductId = 1,
                    TextChunk = "Product: Sample sneaker. Brand: DemoBrand."
                }
            ]);
        _chatCompletionService
            .Setup(s => s.GetStreamingChatMessageContentsAsync(
                It.IsAny<ChatHistory>(),
                It.IsAny<PromptExecutionSettings>(),
                It.IsAny<Kernel?>(),
                It.IsAny<CancellationToken>()))
            .Returns(BuildStreamingResponse("Product ", "info"));

        // Act
        var result =
            await _service.ChatAskAboutProductsAsync(sessionPublicId, request, publicUserId, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);

        var output = string.Empty;
        await foreach (var chunk in result.Value)
        {
            output += chunk;
            if (output == "Product info") break;
        }

        Assert.Equal("Product info", output);
        _chatMessageRepository.Verify(r => r.Add(It.Is<ChatMessage>(m =>
            m.Role == ChatBotRole.User && m.Content == request.Content && m.SessionId == sessionId)), Times.Once);
        _chatMessageRepository.Verify(r => r.Add(It.Is<ChatMessage>(m =>
            m.Role == ChatBotRole.Assistant && m.SessionId == sessionId)), Times.Once);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task ChatAskAboutProductsAsync_WhenNoProducts_ReturnsEmptyInventoryStream()
    {
        // Arrange
        var request = new ChatMessageRequestDto("Any shoes available?");
        var sessionPublicId = Guid.NewGuid();
        var publicUserId = Guid.NewGuid();
        const int sessionId = 33;

        _userRepository
            .Setup(r => r.GetUserIdByPublicIdAsync(publicUserId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(1);
        _chatSessionRepository
            .Setup(r => r.GetChatSessionIdByPublicIdAsync(sessionPublicId, It.IsAny<int>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync(sessionId);
        _chatMessageRepository
            .Setup(r => r.GetHistoryChatMessageAsync(sessionId, It.IsAny<CancellationToken>()))
            .ReturnsAsync(new List<ChatMessage>());
        _embeddingGenerator
            .Setup(g => g.GenerateAsync(
                It.IsAny<IEnumerable<string>>(),
                It.IsAny<EmbeddingGenerationOptions?>(),
                It.IsAny<CancellationToken>()))
            .ReturnsAsync([
                new Embedding<float>(new ReadOnlyMemory<float>(new[] { 0.05f, 0.15f }))
            ]);
        _productEmbeddingRepository
            .Setup(r => r.GetTop5ProductByVectorAsync(It.IsAny<ReadOnlyMemory<float>>(), It.IsAny<CancellationToken>()))
            .ReturnsAsync([]);
        _chatCompletionService
            .Setup(s => s.GetStreamingChatMessageContentsAsync(
                It.IsAny<ChatHistory>(),
                It.IsAny<PromptExecutionSettings>(),
                It.IsAny<Kernel?>(),
                It.IsAny<CancellationToken>()))
            .Returns(BuildStreamingResponse("No ", "products"));

        // Act
        var result =
            await _service.ChatAskAboutProductsAsync(sessionPublicId, request, publicUserId, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);

        var output = string.Empty;
        await foreach (var chunk in result.Value)
        {
            output += chunk;
            if (output == "No products") break;
        }

        Assert.Equal("No products", output);
        _chatMessageRepository.Verify(r => r.Add(It.Is<ChatMessage>(m =>
            m.Role == ChatBotRole.User && m.Content == request.Content && m.SessionId == sessionId)), Times.Once);
        _chatMessageRepository.Verify(r => r.Add(It.Is<ChatMessage>(m =>
            m.Role == ChatBotRole.Assistant && m.SessionId == sessionId)), Times.Once);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
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