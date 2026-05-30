using System.Runtime.CompilerServices;
using System.Text;
using ErrorOr;
using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.ChatCompletion;
using Microsoft.SemanticKernel.Connectors.OpenAI;
using ShoeStore.Application.Constants;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.StatisticsInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Domain.Enum;
using ChatMessage = ShoeStore.Domain.Entities.ChatMessage;

namespace ShoeStore.Application.Services;

public class ChatBotService(
    IStatisticsService statisticsService,
    IChatCompletionService chatCompletionService,
    IChatMessageRepository chatMessageRepository,
    IChatSessionRepository chatSessionRepository,
    IUnitOfWork unitOfWork,
    IUserRepository userRepository,
    IUpdateTitleQueue queue,
    Kernel kernel,
    IProductPluginService productPluginService,
    IMasterDataPluginService masterDataPluginService,
    IStoreAssistantPluginService storeAssistantPluginService)
    : IChatBotService
{
    public async Task<ErrorOr<IAsyncEnumerable<string>>> GenerateCampaignAsync(CreateCampaignRequestDto requestDto,
        Guid publicUserId, CancellationToken token)
    {
        var summaryData = await GetSummaryData(token);
        if (summaryData.IsError) return summaryData.Errors;
        var top3Products = await GetTopProductsData(token);
        if (top3Products.IsError) return top3Products.Errors;

        var userId = await userRepository.GetUserIdByPublicIdAsync(publicUserId, token);
        if (userId == null) return Error.NotFound("User.NotFound", "User not found");

        var sessionId =
            await chatSessionRepository.GetChatSessionIdByPublicIdAsync(requestDto.PublicSessionId, userId.Value,
                token);
        if (sessionId == null) return Error.NotFound("ChatSession.NotFound", "Chat session not found");
        var executionSetting = BuildExecutionSettings();
        IAsyncEnumerable<StreamingChatMessageContent> response;
        var newUserMessage = new ChatMessage
        {
            Content = requestDto.Content,
            Role = ChatBotRole.User,
            SessionId = sessionId.Value,
            CreatedAt = DateTime.UtcNow,
            TokenCount = requestDto.Content.Length / 2 // Rough token estimation
        };
        chatMessageRepository.Add(newUserMessage);
        var totalRevenue = summaryData.Value.TotalRevenue;
        var totalOrders = summaryData.Value.TotalOrders;
        if (totalRevenue <= 0 && totalOrders == 0 && top3Products.Value.Count == 0)
        {
            var emptyStatisticsPrompt = SystemPrompt.GenerateEmptyStatisticsPrompt(true);
            var emptyStatisticsChat = new ChatHistory(emptyStatisticsPrompt);
            emptyStatisticsChat.AddUserMessage(requestDto.Content);
            response = chatCompletionService.GetStreamingChatMessageContentsAsync(
                emptyStatisticsChat,
                executionSetting,
                kernel,
                token);
        }
        else
        {
            var top1 = top3Products.Value.Count > 0
                ? $"{top3Products.Value[0].ProductName} - Revenue: {top3Products.Value[0].TotalRevenue} VND - Total invoices for the product: {top3Products.Value[0].TotalInvoices}"
                : "Updating";
            var top2 = top3Products.Value.Count > 1
                ? $"{top3Products.Value[1].ProductName} - Revenue: {top3Products.Value[1].TotalRevenue} VND - Total invoices for the product: {top3Products.Value[1].TotalInvoices}"
                : "Updating";
            var top3 = top3Products.Value.Count > 2
                ? $"{top3Products.Value[2].ProductName} - Revenue: {top3Products.Value[2].TotalRevenue} VND - Total invoices for the product: {top3Products.Value[2].TotalInvoices}"
                : "Updating";

            var systemPrompt = SystemPrompt.GenerateStatisticsPrompt(totalRevenue, totalOrders, top1, top2, top3, true);
            var chat = new ChatHistory(systemPrompt);
            chat.AddUserMessage(requestDto.Content);

            response =
                chatCompletionService.GetStreamingChatMessageContentsAsync(
                    chat,
                    executionSetting,
                    kernel,
                    token);
        }

        Func<string, Task> onCompleted = async botResponse =>
        {
            await queue.EnqueueAsync(new UpdateTitleRequestDto
            (
                requestDto.PublicSessionId,
                botResponse,
                userId.Value,
                true
            ), token);
        };

        return ErrorOrFactory.From(GenerateAnswerAsync(response, sessionId.Value, onCompleted, token));
    }

    public async Task<ErrorOr<IAsyncEnumerable<string>>> ChatAskAboutStatisticsAsync(Guid publicSessionId,
        ChatMessageRequestDto messageRequestDto, Guid publicUserId, CancellationToken token)
    {
        var summaryData = await GetSummaryData(token);
        if (summaryData.IsError) return summaryData.Errors;
        var top3Products = await GetTopProductsData(token);
        if (top3Products.IsError) return top3Products.Errors;
        var userId = await userRepository.GetUserIdByPublicIdAsync(publicUserId, token);
        if (userId == null) return Error.NotFound("User.NotFound", "User not found");
        var sessionId =
            await chatSessionRepository.GetChatSessionIdByPublicIdAsync(publicSessionId, userId.Value, token);
        if (sessionId == null) return Error.NotFound("ChatSession.NotFound", "Chat session not found");
        var historyChat = await chatMessageRepository.GetHistoryChatMessageAsync(sessionId.Value, token);
        var isFirstMessage = historyChat.Count == 0;
        var totalRevenue = summaryData.Value.TotalRevenue;
        var totalOrders = summaryData.Value.TotalOrders;
        var executionSetting = BuildExecutionSettings();
        IAsyncEnumerable<StreamingChatMessageContent> response;
        var newChatMessage = new ChatMessage
        {
            Content = messageRequestDto.Content,
            Role = ChatBotRole.User,
            SessionId = sessionId.Value,
            CreatedAt = DateTime.UtcNow,
            TokenCount = messageRequestDto.Content.Length / 2 // Rough token estimation for Vietnamese
        };
        chatMessageRepository.Add(newChatMessage);
        if (totalRevenue <= 0 && totalOrders == 0 && top3Products.Value.Count == 0)
        {
            var emptyStatisticsPrompt = SystemPrompt.GenerateEmptyStatisticsPrompt(false);
            var emptyStatisticsChat = new ChatHistory(emptyStatisticsPrompt);
            emptyStatisticsChat.AddUserMessage(messageRequestDto.Content);
            response = chatCompletionService.GetStreamingChatMessageContentsAsync(
                emptyStatisticsChat,
                executionSetting,
                kernel,
                token);
        }
        else
        {
            var top1 = top3Products.Value.Count > 0
                ? $"{top3Products.Value[0].ProductName} - Revenue: {top3Products.Value[0].TotalRevenue} VND - Total invoices for the product: {top3Products.Value[0].TotalInvoices}"
                : "Updating";
            var top2 = top3Products.Value.Count > 1
                ? $"{top3Products.Value[1].ProductName} - Revenue: {top3Products.Value[1].TotalRevenue} VND - Total invoices for the product: {top3Products.Value[1].TotalInvoices}"
                : "Updating";
            var top3 = top3Products.Value.Count > 2
                ? $"{top3Products.Value[2].ProductName} - Revenue: {top3Products.Value[2].TotalRevenue} VND - Total invoices for the product: {top3Products.Value[2].TotalInvoices}"
                : "Updating";
            var reverseHistoryChat = historyChat.OrderBy(m => m.CreatedAt)
                .Select(c => new
                {
                    c.Content,
                    c.Role
                })
                .ToList();

            var systemPrompt =
                SystemPrompt.GenerateStatisticsPrompt(totalRevenue, totalOrders, top1, top2, top3, false);

            var chat = new ChatHistory(systemPrompt);
            var reducer = new TokenBudgetChatReducer(3000);

            foreach (var message in reverseHistoryChat)
                switch (message.Role)
                {
                    case ChatBotRole.Assistant:
                    {
                        chat.AddAssistantMessage(message.Content);
                        break;
                    }
                    case ChatBotRole.User:
                    {
                        chat.AddUserMessage(message.Content);
                        break;
                    }
                }

            var reducedMessage = await reducer.ReduceAsync(chat, token);
            if (reducedMessage != null) chat = new ChatHistory(reducedMessage);

            chat.AddUserMessage(messageRequestDto.Content);

            response =
                chatCompletionService.GetStreamingChatMessageContentsAsync(
                    chat,
                    executionSetting,
                    kernel,
                    token);
        }

        Func<string, Task>? onCompleted = null;
        if (isFirstMessage)
            onCompleted = async _ =>
            {
                await queue.EnqueueAsync(new UpdateTitleRequestDto
                (
                    publicSessionId,
                    messageRequestDto.Content,
                    userId.Value
                ), token);
            };
        return ErrorOrFactory.From(GenerateAnswerAsync(response, sessionId.Value, onCompleted, token));
    }

    public async Task<ErrorOr<IAsyncEnumerable<string>>> ChatAskAboutProductsAsync(Guid publicSessionId,
        ChatMessageRequestDto messageRequestDto, Guid publicUserId, CancellationToken token)
    {
        var userId = await userRepository.GetUserIdByPublicIdAsync(publicUserId, token);
        if (userId == null) return Error.NotFound("User.NotFound", "User not found");
        var sessionId =
            await chatSessionRepository.GetChatSessionIdByPublicIdAsync(publicSessionId, userId.Value, token);
        if (sessionId == null) return Error.NotFound("ChatSession.NotFound", "Chat session not found");
        var historyChat = await chatMessageRepository.GetHistoryChatMessageAsync(sessionId.Value, token);
        var isFirstMessage = historyChat.Count == 0;
        var reverseHistoryChat = historyChat.OrderBy(m => m.CreatedAt)
            .Select(c => new
            {
                c.Content,
                c.Role
            })
            .ToList();
        var ragKernel = kernel.Clone();
        ragKernel.Plugins.AddFromObject(storeAssistantPluginService);
        var newChatMessage = new ChatMessage
        {
            Content = messageRequestDto.Content,
            Role = ChatBotRole.User,
            SessionId = sessionId.Value,
            CreatedAt = DateTime.UtcNow,
            TokenCount = messageRequestDto.Content.Length / 2 // Rough token estimation for Vietnamese
        };
        chatMessageRepository.Add(newChatMessage);
        var executionSetting = BuildStoreAssistantPluginExecutionSettings();
        var systemPrompt = SystemPrompt.GenerateProductPrompt();
        var chat = new ChatHistory(systemPrompt);
        var reducer = new TokenBudgetChatReducer(3000);
        foreach (var message in reverseHistoryChat)
            switch (message.Role)
            {
                case ChatBotRole.Assistant:
                {
                    chat.AddAssistantMessage(message.Content);
                    break;
                }
                case ChatBotRole.User:
                {
                    chat.AddUserMessage(message.Content);
                    break;
                }
            }

        var reducedMessage = await reducer.ReduceAsync(chat, token);
        if (reducedMessage != null) chat = new ChatHistory(reducedMessage);
        chat.AddUserMessage(messageRequestDto.Content);

        var response = chatCompletionService.GetStreamingChatMessageContentsAsync(
            chat, executionSetting, ragKernel, token);

        Func<string, Task>? onCompleted = null;
        if (isFirstMessage)
            onCompleted = async _ =>
            {
                await queue.EnqueueAsync(new UpdateTitleRequestDto
                (
                    publicSessionId,
                    messageRequestDto.Content,
                    userId.Value
                ), token);
            };
        return ErrorOrFactory.From(GenerateAnswerAsync(response, sessionId.Value, onCompleted, token));
    }

    public async Task<ErrorOr<IAsyncEnumerable<string>>> ChatAskAboutProductsForAdminAsync(Guid publicSessionId,
        ChatMessageRequestDto messageRequestDto, Guid publicUserId,
        CancellationToken token)
    {
        var userId = await userRepository.GetUserIdByPublicIdAsync(publicUserId, token);
        if (userId == null) return Error.NotFound("User.NotFound", "User not found");
        var sessionId =
            await chatSessionRepository.GetChatSessionIdByPublicIdAsync(publicSessionId, userId.Value, token);
        if (sessionId == null) return Error.NotFound("ChatSession.NotFound", "Chat session not found");
        var historyChat = await chatMessageRepository.GetHistoryChatMessageAsync(sessionId.Value, token);
        var isFirstMessage = historyChat.Count == 0;
        var reverseHistoryChat = historyChat.OrderBy(m => m.CreatedAt)
            .Select(c => new
            {
                c.Content,
                c.Role
            })
            .ToList();
        var adminKernel = kernel.Clone();
        var executionSetting = BuildPluginExecutionSettings();
        adminKernel.Plugins.AddFromObject(productPluginService);
        adminKernel.Plugins.AddFromObject(masterDataPluginService);
        var newChatMessage = new ChatMessage
        {
            Content = messageRequestDto.Content,
            Role = ChatBotRole.User,
            SessionId = sessionId.Value,
            CreatedAt = DateTime.UtcNow,
            TokenCount = messageRequestDto.Content.Length / 2 // Rough token estimation for Vietnamese
        };
        chatMessageRepository.Add(newChatMessage);
        var systemPrompt = SystemPrompt.GenerateProductAdminSystemPrompt();
        var chat = new ChatHistory(systemPrompt);
        var reducer = new TokenBudgetChatReducer(3000);
        foreach (var message in reverseHistoryChat)
            switch (message.Role)
            {
                case ChatBotRole.Assistant:
                {
                    chat.AddAssistantMessage(message.Content);
                    break;
                }
                case ChatBotRole.User:
                {
                    chat.AddUserMessage(message.Content);
                    break;
                }
            }

        var reducedMessage = await reducer.ReduceAsync(chat, token);
        if (reducedMessage != null) chat = new ChatHistory(reducedMessage);
        chat.AddUserMessage(messageRequestDto.Content);
        var response =
            chatCompletionService.GetStreamingChatMessageContentsAsync(
                chat,
                executionSetting,
                adminKernel,
                token);

        Func<string, Task>? onCompleted = null;
        if (isFirstMessage)
            onCompleted = async _ =>
            {
                await queue.EnqueueAsync(new UpdateTitleRequestDto
                (
                    publicSessionId,
                    messageRequestDto.Content,
                    userId.Value
                ), token);
            };
        return ErrorOrFactory.From(GenerateAnswerAsync(response, sessionId.Value, onCompleted, token));
    }

    private async Task<ErrorOr<StatisticsSummaryResponseDto>> GetSummaryData(CancellationToken token)
    {
        return await statisticsService.GetStatisticsSummaryAsync(token);
    }

    private async Task<ErrorOr<List<ProductHighestStatisticsResponseDto>>> GetTopProductsData(CancellationToken token)
    {
        return await statisticsService.GetProductsHighestStatisticsAsync(token);
    }

    private async IAsyncEnumerable<string> GenerateAnswerAsync(
        IAsyncEnumerable<StreamingChatMessageContent> response, int sessionId, Func<string, Task>? onStreamCompleted,
        [EnumeratorCancellation] CancellationToken token)
    {
        var message = new StringBuilder();
        try
        {
            await foreach (var chunk in response.WithCancellation(token))
                if (!string.IsNullOrEmpty(chunk.Content))
                {
                    yield return chunk.Content;
                    message.Append(chunk.Content);
                }

            yield return "\n";
        }
        finally
        {
            var completeBotResponse = message.ToString();
            var newAssistantMessage = new ChatMessage
            {
                Content = completeBotResponse,
                SessionId = sessionId,
                CreatedAt = DateTime.UtcNow,
                TokenCount = message.Length / 4, // Rough token estimation
                Role = ChatBotRole.Assistant
            };
            chatMessageRepository.Add(newAssistantMessage);
            await unitOfWork.SaveChangesAsync(token);
            if (onStreamCompleted != null) await onStreamCompleted(completeBotResponse);
        }
    }

    private static OpenAIPromptExecutionSettings BuildExecutionSettings()
    {
        return new OpenAIPromptExecutionSettings
        {
            MaxTokens = 500, // Limit response length
            Temperature = 0.6 // Adjust creativity
        };
    }

    private static OpenAIPromptExecutionSettings BuildPluginExecutionSettings()
    {
        return new OpenAIPromptExecutionSettings
        {
            MaxTokens = 1200, // Limit response length
            Temperature = 0.1, // Adjust creativity
            ToolCallBehavior = ToolCallBehavior.AutoInvokeKernelFunctions // Allow tool calls
        };
    }

    private static OpenAIPromptExecutionSettings BuildStoreAssistantPluginExecutionSettings()
    {
        return new OpenAIPromptExecutionSettings
        {
            MaxTokens = 1000,
            Temperature = 0.2,
            ToolCallBehavior = ToolCallBehavior.AutoInvokeKernelFunctions
        };
    }
}