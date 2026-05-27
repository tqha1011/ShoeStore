using System.ComponentModel;
using System.Text;
using Microsoft.Extensions.AI;
using Microsoft.SemanticKernel;
using ShoeStore.Application.Interface.ChatBotInterface;

namespace ShoeStore.Application.Plugin;

public class StoreAssistantPluginService(
    IEmbeddingGenerator<string, Embedding<float>> embeddingGenerator,
    IProductEmbeddingRepository productEmbeddingRepository)
    : IStoreAssistantPluginService
{
    [KernelFunction("search-store-inventory")]
    [Description("Searches the store's inventory for shoes based on customer needs, styles, or specific requests. " +
                 "CRITICAL: Call this function ONLY when the user explicitly asks for shoe recommendations, checks availability, or asks about product details. " +
                 "DO NOT call this function for general greetings (Hello, Hi) or small talk.")]
    public async Task<string> SearchInventory(string keyword, CancellationToken token)
    {
        var queryVector = await embeddingGenerator.GenerateAsync(keyword, cancellationToken: token);
        var top5Products = await productEmbeddingRepository.GetTop5ProductByVectorAsync(queryVector.Vector, token);
        if (top5Products.Count == 0) return "Hệ thống kho đang nâng cấp hoặc không tìm thấy sản phẩm phù hợp.";

        var context = new StringBuilder();
        foreach (var product in top5Products) context.Append($"- {product.TextChunk}\n");

        return context.ToString();
    }
}