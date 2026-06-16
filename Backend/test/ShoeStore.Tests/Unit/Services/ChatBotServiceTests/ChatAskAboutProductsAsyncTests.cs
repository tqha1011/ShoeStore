using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.ChatCompletion;
using Moq;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.StatisticsInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Services;
using ShoeStore.Domain.Enum;
using ChatMessage = ShoeStore.Domain.Entities.ChatMessage;

namespace ShoeStore.Tests.Unit.Services.ChatBotServiceTests;

public class ChatAskAboutProductsAsyncTests
{
    private readonly Mock<IChatCompletionService> _chatCompletionService = new();
    private readonly Mock<IChatMessageRepository> _chatMessageRepository = new();
    private readonly Mock<IChatSessionRepository> _chatSessionRepository = new();
    private readonly Kernel _kernel = Kernel.CreateBuilder().Build();
    private readonly Mock<IMasterDataPluginService> _masterDataPluginService = new();
    private readonly Mock<IProductPluginService> _productPluginService = new();
    private readonly Mock<IUpdateTitleQueue> _queue = new();
    private readonly ChatBotService _service;
    private readonly Mock<IStatisticsService> _statisticsService = new();
    private readonly Mock<IStoreAssistantPluginService> _storeAssistantPluginService = new();
    private readonly Mock<IUnitOfWork> _unitOfWork = new();
    private readonly Mock<IUserRepository> _userRepository = new();
    private readonly Mock<IInvoicePluginService> _invoicePluginService = new();

    public ChatAskAboutProductsAsyncTests()
    {
        _service = new ChatBotService(
            _statisticsService.Object,
            _chatCompletionService.Object,
            _chatMessageRepository.Object,
            _chatSessionRepository.Object,
            _unitOfWork.Object,
            _userRepository.Object,
            _queue.Object,
            _kernel,
            _productPluginService.Object,
            _masterDataPluginService.Object,
            _storeAssistantPluginService.Object,
            _invoicePluginService.Object);
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
    public async Task ChatAskAboutProductsAsync_WhenValidRequest_ReturnsStreamAndPersistsMessages()
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
        _storeAssistantPluginService
            .Setup(s => s.SearchInventory(request.Content, It.IsAny<CancellationToken>()))
            .ReturnsAsync("""
                          SearchResult: NoMatch
                          Instruction: No inventory item matched the user's request strongly enough.
                          """);
        _chatCompletionService
            .Setup(s => s.GetStreamingChatMessageContentsAsync(
                It.IsAny<ChatHistory>(),
                It.IsAny<PromptExecutionSettings>(),
                It.IsAny<Kernel>(),
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
        _storeAssistantPluginService.Verify(s => s.SearchInventory(request.Content, It.IsAny<CancellationToken>()),
            Times.Once);
        _unitOfWork.Verify(u => u.SaveChangesAsync(It.IsAny<CancellationToken>()), Times.Once);
    }

    [Fact]
    public async Task ChatAskAboutProductsAsync_WhenProductQuestion_AddsInventoryContextToPrompt()
    {
        // Arrange
        var request = new ChatMessageRequestDto("Có giày chạy bộ size 42 không?");
        var sessionPublicId = Guid.NewGuid();
        var publicUserId = Guid.NewGuid();
        const int sessionId = 34;
        ChatHistory? capturedChat = null;

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
        _storeAssistantPluginService
            .Setup(s => s.SearchInventory(request.Content, It.IsAny<CancellationToken>()))
            .ReturnsAsync("""
                          SearchResult: Found
                          AllowedProductNames: Runner Pro

                          Product: Runner Pro
                          RecommendationEligibility: CanRecommend
                          Availability: In stock
                          In-stock variants:
                          - Color: Black, Sizes: 42, Price: 1200000 VND, Status: In stock
                          """);
        _chatCompletionService
            .Setup(s => s.GetStreamingChatMessageContentsAsync(
                It.IsAny<ChatHistory>(),
                It.IsAny<PromptExecutionSettings>(),
                It.IsAny<Kernel>(),
                It.IsAny<CancellationToken>()))
            .Callback<ChatHistory, PromptExecutionSettings, Kernel, CancellationToken>((chat, _, _, _) =>
                capturedChat = chat)
            .Returns(BuildStreamingResponse("Có Runner Pro"));

        // Act
        var result =
            await _service.ChatAskAboutProductsAsync(sessionPublicId, request, publicUserId, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        await DrainAsync(result.Value);

        Assert.NotNull(capturedChat);
        var finalUserMessage = capturedChat.Last().Content;
        Assert.Contains("[CURRENT USER MESSAGE]", finalUserMessage);
        Assert.Contains(request.Content, finalUserMessage);
        Assert.Contains("[CURRENT INVENTORY CONTEXT]", finalUserMessage);
        Assert.Contains("SearchResult: Found", finalUserMessage);
        Assert.Contains("AllowedProductNames: Runner Pro", finalUserMessage);
        Assert.Contains("RecommendationEligibility: CanRecommend", finalUserMessage);
    }

    [Fact]
    public async Task ChatAskAboutProductsAsync_WhenSmallTalkOnly_DoesNotSearchInventory()
    {
        // Arrange
        var request = new ChatMessageRequestDto("Chào shop");
        var sessionPublicId = Guid.NewGuid();
        var publicUserId = Guid.NewGuid();
        const int sessionId = 35;
        ChatHistory? capturedChat = null;

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
        _chatCompletionService
            .Setup(s => s.GetStreamingChatMessageContentsAsync(
                It.IsAny<ChatHistory>(),
                It.IsAny<PromptExecutionSettings>(),
                It.IsAny<Kernel>(),
                It.IsAny<CancellationToken>()))
            .Callback<ChatHistory, PromptExecutionSettings, Kernel, CancellationToken>((chat, _, _, _) =>
                capturedChat = chat)
            .Returns(BuildStreamingResponse("Chào anh/chị"));

        // Act
        var result =
            await _service.ChatAskAboutProductsAsync(sessionPublicId, request, publicUserId, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        await DrainAsync(result.Value);

        _storeAssistantPluginService.Verify(s => s.SearchInventory(It.IsAny<string>(), It.IsAny<CancellationToken>()),
            Times.Never);
        Assert.NotNull(capturedChat);
        var finalUserMessage = capturedChat.Last().Content;
        Assert.Equal(request.Content, finalUserMessage);
    }

    [Fact]
    public async Task ChatAskAboutProductsAsync_WhenFollowUpQuestion_SearchesWithRecentHistory()
    {
        // Arrange
        var request = new ChatMessageRequestDto("Còn size 42 không?");
        var sessionPublicId = Guid.NewGuid();
        var publicUserId = Guid.NewGuid();
        const int sessionId = 36;
        string? capturedKeyword = null;
        var history = new List<ChatMessage>
        {
            new()
            {
                Content = "Mình muốn giày chạy bộ",
                Role = ChatBotRole.User,
                SessionId = sessionId,
                CreatedAt = DateTime.UtcNow.AddMinutes(-2),
                TokenCount = 10
            },
            new()
            {
                Content = "Shop có Runner Pro khá phù hợp",
                Role = ChatBotRole.Assistant,
                SessionId = sessionId,
                CreatedAt = DateTime.UtcNow.AddMinutes(-1),
                TokenCount = 10
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
        _storeAssistantPluginService
            .Setup(s => s.SearchInventory(It.IsAny<string>(), It.IsAny<CancellationToken>()))
            .Callback<string, CancellationToken>((keyword, _) => capturedKeyword = keyword)
            .ReturnsAsync("""
                          SearchResult: Found
                          AllowedProductNames: Runner Pro
                          Product: Runner Pro
                          RecommendationEligibility: CanRecommend
                          """);
        _chatCompletionService
            .Setup(s => s.GetStreamingChatMessageContentsAsync(
                It.IsAny<ChatHistory>(),
                It.IsAny<PromptExecutionSettings>(),
                It.IsAny<Kernel>(),
                It.IsAny<CancellationToken>()))
            .Returns(BuildStreamingResponse("Có size 42"));

        // Act
        var result =
            await _service.ChatAskAboutProductsAsync(sessionPublicId, request, publicUserId, CancellationToken.None);

        // Assert
        Assert.False(result.IsError);
        await DrainAsync(result.Value);

        Assert.NotNull(capturedKeyword);
        Assert.Contains("User: Mình muốn giày chạy bộ", capturedKeyword);
        Assert.Contains("Assistant: Shop có Runner Pro khá phù hợp", capturedKeyword);
        Assert.Contains(request.Content, capturedKeyword);
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

    private static async Task DrainAsync(IAsyncEnumerable<string> stream)
    {
        await foreach (var _ in stream)
        {
        }
    }
}
