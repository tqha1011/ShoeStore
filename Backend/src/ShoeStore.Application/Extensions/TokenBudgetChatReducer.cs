using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.ChatCompletion;

namespace ShoeStore.Application.Extensions;

public class TokenBudgetChatReducer(int maxToken) : IChatHistoryReducer
{
    public Task<IEnumerable<ChatMessageContent>?> ReduceAsync(IReadOnlyList<ChatMessageContent>? chatHistory,
        CancellationToken cancellationToken = new())
    {
        if (chatHistory == null || chatHistory.Count == 0)
            return Task.FromResult<IEnumerable<ChatMessageContent>?>(null);

        var currentToken = CalculateCurrentToken(chatHistory);

        if (currentToken <= maxToken)
            return Task.FromResult<IEnumerable<ChatMessageContent>?>(null);

        var reducedChatHistory = new List<ChatMessageContent>();
        var availableTokenBudget = maxToken;

        var hasSystemPrompt = chatHistory[0].Role == AuthorRole.System;
        if (hasSystemPrompt)
        {
            var systemToken = CalculateMessageToken(chatHistory[0]);
            availableTokenBudget -= systemToken;
        }

        availableTokenBudget = Math.Max(0, availableTokenBudget);

        var totalToken = 0;
        var startIndex = hasSystemPrompt ? 1 : 0;

        var keepNextAssistant = false;

        for (var i = chatHistory.Count - 1; i >= startIndex; i--)
        {
            var message = chatHistory[i];
            var estimatedToken = CalculateMessageToken(message);

            if (totalToken + estimatedToken > availableTokenBudget && !keepNextAssistant)
                break;

            totalToken += estimatedToken;
            reducedChatHistory.Insert(0, message);


            if (message.Role == AuthorRole.Tool)
                keepNextAssistant = true;

            else if (message.Role == AuthorRole.Assistant && keepNextAssistant) keepNextAssistant = false;
        }

        if (hasSystemPrompt) reducedChatHistory.Insert(0, chatHistory[0]);

        return Task.FromResult<IEnumerable<ChatMessageContent>?>(reducedChatHistory);
    }

    private static int CalculateCurrentToken(IReadOnlyList<ChatMessageContent> chatHistory)
    {
        var sumTokens = 0;
        foreach (var message in chatHistory) sumTokens += CalculateMessageToken(message);
        return sumTokens;
    }


    private static int CalculateMessageToken(ChatMessageContent message)
    {
        var token = 0;


        if (!string.IsNullOrEmpty(message.Content)) token += message.Content.Length / 2;


        if (message.Items.Any())
            foreach (var item in message.Items)
                if (item is FunctionCallContent functionCall)
                {
                    token += (functionCall.PluginName?.Length ?? 0) / 2;
                    token += (functionCall.FunctionName?.Length ?? 0) / 2;
                    token += (functionCall.Arguments?.ToString()?.Length ?? 0) / 2;
                }
                else if (item is FunctionResultContent functionResult)
                {
                    token += (functionResult.PluginName?.Length ?? 0) / 2;
                    token += (functionResult.FunctionName?.Length ?? 0) / 2;
                    token += (functionResult.Result?.ToString()?.Length ?? 0) / 2;
                }

        return token;
    }
}