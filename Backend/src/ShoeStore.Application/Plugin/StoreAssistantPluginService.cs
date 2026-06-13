using System.ComponentModel;
using System.Text;
using Microsoft.Extensions.AI;
using Microsoft.Extensions.Logging;
using Microsoft.SemanticKernel;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Entities.Embedding;

namespace ShoeStore.Application.Plugin;

public class StoreAssistantPluginService(
    IEmbeddingGenerator<string, Embedding<float>> embeddingGenerator,
    IProductEmbeddingRepository productEmbeddingRepository,
    IProductRepository productRepository,
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

            var top5ProductEmbeddings =
                await productEmbeddingRepository.GetTop5ProductByVectorAsync(queryVector.Vector, token);
            if (top5ProductEmbeddings.Count == 0)
                return
                    "Hiện tại không tìm thấy sản phẩm phù hợp với yêu cầu của bạn. Vui lòng thử lại với từ khóa khác hoặc cung cấp thêm chi tiết về sản phẩm bạn đang tìm kiếm.";

            var productIds = top5ProductEmbeddings
                .Select(x => x.ProductId)
                .Distinct()
                .ToList();
            var products = await productRepository.GetProductsForRagInventoryAsync(productIds, token);
            if (products.Count == 0)
                return
                    "Hiện tại không tìm thấy sản phẩm phù hợp với yêu cầu của bạn. Vui lòng thử lại với từ khóa khác hoặc cung cấp thêm chi tiết về sản phẩm bạn đang tìm kiếm.";

            var productById = products.ToDictionary(x => x.Id);
            var context = new StringBuilder();
            foreach (var productEmbedding in top5ProductEmbeddings)
            {
                if (!productById.TryGetValue(productEmbedding.ProductId, out var product)) continue;
                context.AppendLine(BuildInventoryContext(product));
            }

            return context.ToString();
        }
        catch (Exception ex)
        {
            logger.LogError(ex, "Error occurred while searching store inventory with keyword: {Keyword}", keyword);
            return "Hệ thống tìm kiếm đang gặp sự cố. Vui lòng thử lại sau.";
        }
    }

    private static string BuildInventoryContext(Product product)
    {
        var variants = product.ProductVariants
            .Where(v => v.IsSelling && !v.IsDeleted)
            .ToList();
        var availableVariants = variants
            .Where(v => v.Stock > 0)
            .GroupBy(v => new
            {
                ColorName = v.Color?.ColorName ?? "Unknown",
                v.Price
            })
            .Select(group => new
            {
                group.Key.ColorName,
                group.Key.Price,
                Sizes = group
                    .OrderBy(v => v.Size?.Size)
                    .Select(v => FormatSize(v.Size?.Size))
                    .Distinct()
                    .ToList()
            })
            .ToList();
        var unavailableVariants = variants
            .Where(v => v.Stock <= 0)
            .GroupBy(v => new
            {
                ColorName = v.Color?.ColorName ?? "Unknown",
                v.Price
            })
            .Select(group => new
            {
                group.Key.ColorName,
                group.Key.Price,
                Sizes = group
                    .OrderBy(v => v.Size?.Size)
                    .Select(v => FormatSize(v.Size?.Size))
                    .Distinct()
                    .ToList()
            })
            .ToList();

        var context = new StringBuilder();
        context.AppendLine($"Product: {product.ProductName}");
        context.AppendLine($"Description: {product.Description ?? "No description"}");
        context.AppendLine($"Brand: {product.Brand ?? "Unknown"}");
        context.AppendLine($"Category: {product.Category?.Name ?? "Unknown"}");

        if (availableVariants.Count > 0)
        {
            context.AppendLine("Availability: In stock");
            context.AppendLine("In-stock variants:");
            foreach (var variant in availableVariants)
                context.AppendLine(
                    $"- Color: {variant.ColorName}, Sizes: {string.Join(", ", variant.Sizes)}, Price: {variant.Price} VND, Status: In stock");
        }
        else
        {
            context.AppendLine("Availability: Out of stock");
        }

        if (unavailableVariants.Count > 0)
        {
            context.AppendLine("Out-of-stock variants:");
            foreach (var variant in unavailableVariants)
                context.AppendLine(
                    $"- Color: {variant.ColorName}, Sizes: {string.Join(", ", variant.Sizes)}, Price: {variant.Price} VND, Status: Out of stock");
        }

        return context.ToString();
    }

    private static string FormatSize(decimal? size)
    {
        return size?.ToString("0.##") ?? "Unknown";
    }
}
