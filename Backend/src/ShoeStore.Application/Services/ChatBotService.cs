using System.Runtime.CompilerServices;
using Microsoft.SemanticKernel.ChatCompletion;
using Microsoft.SemanticKernel.Connectors.OpenAI;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.StatisticsInterface;

namespace ShoeStore.Application.Services;

public class ChatBotService(IStatisticsService statisticsService, IChatCompletionService chatCompletionService)
    : IChatBotService
{
    public async IAsyncEnumerable<string> GenerateCampaignAsync([EnumeratorCancellation] CancellationToken token)
    {
        var summaryData = await statisticsService.GetStatisticsSummaryAsync(token);
        var top3Products = await statisticsService.GetProductsHighestStatisticsAsync(token);

        var totalRevenue = summaryData.Value.TotalRevenue;
        var totalOrders = summaryData.Value.TotalOrders;

        var top1 = top3Products.Value.Count > 0
            ? $"{top3Products.Value[0].ProductName} - Doanh thu: {top3Products.Value[0].TotalRevenue} VND - Tổng hóa đơn của sản phẩm: {top3Products.Value[0].TotalInvoices}"
            : "Đang cập nhật ";
        var top2 = top3Products.Value.Count > 1
            ? $"{top3Products.Value[1].ProductName} - Doanh thu: {top3Products.Value[1].TotalRevenue} VND - Tổng hóa đơn của sản phẩm: {top3Products.Value[1].TotalInvoices}"
            : "Đang cập nhật";
        var top3 = top3Products.Value.Count > 2
            ? $"{top3Products.Value[2].ProductName} - Doanh thu: {top3Products.Value[2].TotalRevenue} VND - Tổng hóa đơn của sản phẩm   : {top3Products.Value[2].TotalInvoices}"
            : "Đang cập nhật";

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

        await foreach (var chunk in response)
            if (!string.IsNullOrEmpty(chunk.Content))
                yield return chunk.Content;
    }
}