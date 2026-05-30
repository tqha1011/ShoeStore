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
            var systemToken = chatHistory[0].Content?.Length / 2 ?? 0;
            availableTokenBudget -= systemToken;
        }
        
        availableTokenBudget = Math.Max(0, availableTokenBudget);

        var totalToken = 0;
        var startIndex = hasSystemPrompt ? 1 : 0; 
        
        for (var i = chatHistory.Count - 1; i >= startIndex; i--)
        {
            var message = chatHistory[i];
            var estimatedToken = message.Content?.Length / 2 ?? 0;
            
            if (totalToken + estimatedToken > availableTokenBudget) 
                break;
                
            totalToken += estimatedToken;
            reducedChatHistory.Insert(0, message);
        }
        
        if (hasSystemPrompt)
        {
            reducedChatHistory.Insert(0, chatHistory[0]);
        }

        return Task.FromResult<IEnumerable<ChatMessageContent>?>(reducedChatHistory);
    }

    private static int CalculateCurrentToken(IReadOnlyList<ChatMessageContent> chatHistory)
    {
        var sumTokens = 0;
        foreach (var message in chatHistory)
        {
            var token = message.Content?.Length / 2 ?? 0;
            sumTokens += token;
        }

        return sumTokens;
    }
}