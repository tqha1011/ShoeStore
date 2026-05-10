using System.Runtime.CompilerServices;
using System.Text;
using ErrorOr;
using Microsoft.Extensions.AI;
using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.ChatCompletion;
using Microsoft.SemanticKernel.Connectors.OpenAI;
using ShoeStore.Application.Constants;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.StatisticsInterface;
using ShoeStore.Domain.Enum;
using ChatMessage = ShoeStore.Domain.Entities.ChatMessage;

namespace ShoeStore.Application.Services;

public class ChatBotService(
    IStatisticsService statisticsService,
    IChatCompletionService chatCompletionService,
    IChatMessageRepository chatMessageRepository,
    IChatSessionRepository chatSessionRepository,
    IUnitOfWork unitOfWork,
    IEmbeddingGenerator<string, Embedding<float>> embeddingGenerator,
    IProductEmbeddingRepository productEmbeddingRepository)
    : IChatBotService
{
    public async Task<ErrorOr<IAsyncEnumerable<string>>> GenerateCampaignAsync(CreateCampaignRequestDto requestDto,
        CancellationToken token)
    {
        var summaryData = await GetSummaryData(token);
        if (summaryData.IsError) return summaryData.Errors;
        var top3Products = await GetTopProductsData(token);
        if (top3Products.IsError) return top3Products.Errors;

        var sessionId = await chatSessionRepository.GetChatSessionIdByPublicIdAsync(requestDto.PublicSessionId, token);
        if (sessionId == null) return Error.NotFound("ChatSession.NotFound", "Chat session not found");

        var newUserMessage = new ChatMessage
        {
            Content = requestDto.Content,
            Role = ChatBotRole.User,
            SessionId = sessionId.Value,
            CreatedAt = DateTime.UtcNow,
            TokenCount = requestDto.Content.Length / 4 // Rough token estimation
        };
        chatMessageRepository.Add(newUserMessage);
        await unitOfWork.SaveChangesAsync(token);

        var totalRevenue = summaryData.Value.TotalRevenue;
        var totalOrders = summaryData.Value.TotalOrders;

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

        var executionSetting = new OpenAIPromptExecutionSettings
        {
            MaxTokens = 500, // Limit response length
            Temperature = 0.6 // Adjust creativity
        };

        var response =
            chatCompletionService.GetStreamingChatMessageContentsAsync(
                chat,
                executionSetting,
                cancellationToken: token);

        return ErrorOrFactory.From(GenerateAnswerAsync(response, sessionId.Value, token));
    }

    public async Task<ErrorOr<IAsyncEnumerable<string>>> ChatAskAboutStatisticsAsync(Guid publicSessionId,
        ChatMessageRequestDto messageRequestDto, CancellationToken token)
    {
        var summaryData = await GetSummaryData(token);
        if (summaryData.IsError) return summaryData.Errors;
        var top3Products = await GetTopProductsData(token);
        if (top3Products.IsError) return top3Products.Errors;
        var sessionId = await chatSessionRepository.GetChatSessionIdByPublicIdAsync(publicSessionId, token);
        if (sessionId == null) return Error.NotFound("ChatSession.NotFound", "Chat session not found");
        var totalRevenue = summaryData.Value.TotalRevenue;
        var totalOrders = summaryData.Value.TotalOrders;

        var top1 = top3Products.Value.Count > 0
            ? $"{top3Products.Value[0].ProductName} - Revenue: {top3Products.Value[0].TotalRevenue} VND - Total invoices for the product: {top3Products.Value[0].TotalInvoices}"
            : "Updating";
        var top2 = top3Products.Value.Count > 1
            ? $"{top3Products.Value[1].ProductName} - Revenue: {top3Products.Value[1].TotalRevenue} VND - Total invoices for the product: {top3Products.Value[1].TotalInvoices}"
            : "Updating";
        var top3 = top3Products.Value.Count > 2
            ? $"{top3Products.Value[2].ProductName} - Revenue: {top3Products.Value[2].TotalRevenue} VND - Total invoices for the product: {top3Products.Value[2].TotalInvoices}"
            : "Updating";
        var historyChat = await chatMessageRepository.GetHistoryChatMessageAsync(sessionId.Value, token);
        var reverseHistoryChat = historyChat.OrderBy(m => m.CreatedAt)
            .Select(c => new
            {
                c.Content,
                c.Role
            })
            .ToList();

        var systemPrompt = SystemPrompt.GenerateStatisticsPrompt(totalRevenue, totalOrders, top1, top2, top3, false);

        var chat = new ChatHistory(systemPrompt);
        var reducer = new ChatHistoryTruncationReducer(20, 35);

        foreach (var message in reverseHistoryChat)
        {
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
        }
        var reducedMessage = await reducer.ReduceAsync(chat, token);
        if (reducedMessage != null) chat = new ChatHistory(reducedMessage);
        
        chat.Add(new ChatMessageContent(AuthorRole.User, messageRequestDto.Content));

        var newChatMessage = new ChatMessage
        {
            Content = messageRequestDto.Content,
            Role = ChatBotRole.User,
            SessionId = sessionId.Value,
            CreatedAt = DateTime.UtcNow,
            TokenCount = messageRequestDto.Content.Length / 4 // Rough token estimation
        };
        chatMessageRepository.Add(newChatMessage);
        await unitOfWork.SaveChangesAsync(token);

        var executionSetting = new OpenAIPromptExecutionSettings
        {
            MaxTokens = 500, // Limit response length
            Temperature = 0.6 // Adjust creativity
        };

        var response =
            chatCompletionService.GetStreamingChatMessageContentsAsync(
                chat,
                executionSetting,
                cancellationToken: token);
        return ErrorOrFactory.From(GenerateAnswerAsync(response, sessionId.Value, token));
    }

    public async Task<ErrorOr<IAsyncEnumerable<string>>> ChatAskAboutProductsAsync(Guid publicSessionId,
        ChatMessageRequestDto messageRequestDto, CancellationToken token)
    {
        var sessionId = await chatSessionRepository.GetChatSessionIdByPublicIdAsync(publicSessionId, token);
        if (sessionId == null) return Error.NotFound("ChatSession.NotFound", "Chat session not found");

        var historyChat = await chatMessageRepository.GetHistoryChatMessageAsync(sessionId.Value, token);
        var reverseHistoryChat = historyChat.OrderBy(m => m.CreatedAt)
            .Select(c => new
            {
                c.Content,
                c.Role
            })
            .ToList();
        var queryVector = await GenerateQueryVectorAsync(messageRequestDto.Content, token);
        var top5Products = await productEmbeddingRepository.GetTop5ProductByVectorAsync(queryVector, token);
        var newChatMessage = new ChatMessage
        {
            Content = messageRequestDto.Content,
            Role = ChatBotRole.User,
            SessionId = sessionId.Value,
            CreatedAt = DateTime.UtcNow,
            TokenCount = messageRequestDto.Content.Length / 4 // Rough token estimation
        };
        chatMessageRepository.Add(newChatMessage);
        var executionSetting = new OpenAIPromptExecutionSettings
        {
            MaxTokens = 500, // Limit response length
            Temperature = 0.6 // Adjust creativity
        };
        IAsyncEnumerable<StreamingChatMessageContent> response;
        if (top5Products.Count == 0)
        {
            var systemEmptyInventoryPrompt = SystemPrompt.GenerateEmptyInventoryPrompt();
            var emptyInventoryChat = new ChatHistory(systemEmptyInventoryPrompt);
            emptyInventoryChat.AddUserMessage(messageRequestDto.Content);
            response =
                chatCompletionService.GetStreamingChatMessageContentsAsync(
                    emptyInventoryChat,
                    executionSetting,
                    cancellationToken: token);
        }
        else
        {
            var context = new StringBuilder();
            foreach (var product in top5Products)
            {
                context.Append($"{product.TextChunk} \n");
            }
            var systemPrompt = SystemPrompt.GenerateProductPrompt(context.ToString());
            var chat = new ChatHistory(systemPrompt);
            var reducer = new ChatHistoryTruncationReducer(20, 35);

            foreach (var message in reverseHistoryChat)
            {
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
            }
            var reducedMessage = await reducer.ReduceAsync(chat, token);
            if (reducedMessage != null) chat = new ChatHistory(reducedMessage);

            chat.AddUserMessage(messageRequestDto.Content);
            response =
                chatCompletionService.GetStreamingChatMessageContentsAsync(
                    chat,
                    executionSetting,
                    cancellationToken: token);
        }
        return ErrorOrFactory.From(GenerateAnswerAsync(response, sessionId.Value, token));
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
        IAsyncEnumerable<StreamingChatMessageContent> response, int sessionId,
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
            var newAssistantMessage = new ChatMessage
            {
                Content = message.ToString(),
                SessionId = sessionId,
                CreatedAt = DateTime.UtcNow,
                TokenCount = 0,
                Role = ChatBotRole.Assistant
            };
            chatMessageRepository.Add(newAssistantMessage);
            await unitOfWork.SaveChangesAsync(token);   
        }
    }

    private async Task<ReadOnlyMemory<float>> GenerateQueryVectorAsync(string content, CancellationToken token)
    {
        var vector = await embeddingGenerator.GenerateAsync(content, cancellationToken: token);
        return vector.Vector;
    }
}