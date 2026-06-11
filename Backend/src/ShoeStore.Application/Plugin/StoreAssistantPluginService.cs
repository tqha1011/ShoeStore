using System.ComponentModel;
using System.Text;
using Microsoft.Extensions.AI;
using Microsoft.Extensions.Logging;
using Microsoft.SemanticKernel;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Domain.Entities.Embedding;

namespace ShoeStore.Application.Plugin;

public class StoreAssistantPluginService(
    IEmbeddingGenerator<string, Embedding<float>> embeddingGenerator,
    IProductEmbeddingRepository productEmbeddingRepository,
    ILogger<StoreAssistantPluginService> logger)
    : IStoreAssistantPluginService
{
    [KernelFunction("search-store-inventory")]
    [Description("Searches the store's inventory for shoes based on customer needs, styles, or specific requests. " +
                 "CRITICAL: Call this function ONLY when the user explicitly asks for shoe recommendations, checks availability, or asks about product details. " +
                 "DO NOT call this function for general greetings (Hello, Hi) or small talk.")]
    public async Task<string> SearchInventory(string keyword, CancellationToken token)
    {
        try
        {
            var queryVector = await embeddingGenerator.GenerateAsync(keyword, cancellationToken: token);
            if (queryVector.Vector.Length != ProductEmbedding.EmbeddingDimensions)
            {
                logger.LogError("Invalid query embedding dimension. Expected {ExpectedDimension}, received {ActualDimension}.",
                    ProductEmbedding.EmbeddingDimensions,
                    queryVector.Vector.Length);
                return "Hệ thống tìm kiếm đang gặp sự cố. Vui lòng thử lại sau.";
            }

            var top5Products = await productEmbeddingRepository.GetTop5ProductByVectorAsync(queryVector.Vector, token);
            if (top5Products.Count == 0)
                return
                    "Hiện tại không tìm thấy sản phẩm phù hợp với yêu cầu của bạn. Vui lòng thử lại với từ khóa khác hoặc cung cấp thêm chi tiết về sản phẩm bạn đang tìm kiếm.";

            var context = new StringBuilder();
            foreach (var product in top5Products) context.Append($"- {product.TextChunk}\n");

            return context.ToString();
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Error occurred while searching store inventory with keyword: {Keyword}", keyword);
            return "Hệ thống tìm kiếm đang gặp sự cố. Vui lòng thử lại sau.";
        }
    }
}
