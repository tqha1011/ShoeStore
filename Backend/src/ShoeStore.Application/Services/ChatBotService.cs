using System.Runtime.CompilerServices;
using System.Text;
using ErrorOr;
using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.ChatCompletion;
using Microsoft.SemanticKernel.Connectors.OpenAI;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.DTOs.StatisticsDto;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.StatisticsInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services;

public class ChatBotService(
    IStatisticsService statisticsService,
    IChatCompletionService chatCompletionService,
    IChatMessageRepository chatMessageRepository,
    IChatSessionRepository chatSessionRepository,
    IUnitOfWork unitOfWork)
    : IChatBotService
{
    public async Task<ErrorOr<IAsyncEnumerable<string>>> GenerateCampaignAsync(CreateCampaignRequestDto requestDto,CancellationToken token)
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

        var systemPrompt = $"""
                            You are the Chief Marketing Officer (CMO) for the store system. You excel at reading data and crafting practical, high-conversion campaigns.

                            Below is the CURRENT BUSINESS REPORT:
                            - Total revenue: {totalRevenue} VND
                            - Total orders: {totalOrders}
                            - Top 3 best-selling products:
                              1. {top1}
                              2. {top2}
                              3. {top3}

                            Task:
                            Based on the numbers above, analyze briefly and propose ONE (01) business/marketing campaign for next month to sustain growth or boost sales.

                            Output format (clear, no extra text):
                            1. CAMPAIGN NAME: [Catchy, attention-grabbing name]
                            2. CORE MESSAGE (Slogan): [Exactly 1 sentence]
                            3. QUICK ANALYSIS: [2 lines on why this fits the data]
                            4. EXECUTION ACTIONS:
                               - [Bullet 1: What to do with Top 3 hot products]
                               - [Bullet 2: Any promotion/combo to increase total orders]

                            Output constraints:
                            - Output only the format above; do not add any other lines.
                            - No greetings, no thanks, no prefaces like "here is my opinion".
                            - No personal opinions or phrases like "I think", "in my view", "my opinion".
                            - No explanation of process or commentary outside the required content.
                            - English only.
                            """;
        var chat = new ChatHistory(systemPrompt);

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

        return ErrorOrFactory.From(GenerateAnswerAsync(response,sessionId.Value, token));
    }

    public async Task<ErrorOr<IAsyncEnumerable<string>>> ChatAskAboutStatisticsAsync(Guid publicSessionId,
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

        const string systemPrompt =
            "You are the Chief Marketing Officer (CMO) for the store system. You excel at reading data and crafting practical, high-conversion campaigns.";

        var chat = new ChatHistory(systemPrompt);

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
        return ErrorOrFactory.From(GenerateAnswerAsync(response,sessionId.Value, token));
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
        IAsyncEnumerable<StreamingChatMessageContent> response,int sessionId,[EnumeratorCancellation] CancellationToken token)
    {
        var message = new StringBuilder();
        await foreach (var chunk in response.WithCancellation(token))
            if (!string.IsNullOrEmpty(chunk.Content))
            {
                yield return chunk.Content;
                message.Append(chunk.Content);
            }
        yield return "\n";

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