using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.ChatCompletion;

namespace ShoeStore.Application.Extensions;

public class TokenBudgetChatReducer(int maxToken) : IChatHistoryReducer
{
    public Task<IEnumerable<ChatMessageContent>?> ReduceAsync(IReadOnlyList<ChatMessageContent> chatHistory,
        CancellationToken cancellationToken = new())
    {
        var totalToken = 0;
        var currentToken = CalculateCurrentToken(chatHistory);
        if (currentToken <= maxToken) return Task.FromResult<IEnumerable<ChatMessageContent>?>(null);
        var reducedChatHistory = new List<ChatMessageContent>();
        foreach (var message in chatHistory.Reverse())
        {
            var estimatedToken = message.Content?.Length / 2 ?? 0; // estimated token for Vietnamese response
            if (totalToken + estimatedToken > maxToken) break;
            totalToken += estimatedToken;
            reducedChatHistory.Insert(0, message);
        }

        return Task.FromResult<IEnumerable<ChatMessageContent>?>(reducedChatHistory);
    }

    private static int CalculateCurrentToken(IReadOnlyList<ChatMessageContent> reverseHistoryChat)
    {
        var sumTokens = 0;
        foreach (var message in reverseHistoryChat)
        {
            var token = message.Content?.Length / 2 ?? 0;
            sumTokens += token;
        }

        return sumTokens;
    }
}