using System.Text;
using ErrorOr;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.AI;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Entities.Embedding;

namespace ShoeStore.Application.Services;

/// <summary>
///     Generates and persists vector embeddings for products to support semantic search.
/// </summary>
public class ProductEmbeddingService(
    IProductRepository productRepository,
    IProductEmbeddingRepository productEmbeddingRepository,
    IUnitOfWork unitOfWork,
    IEmbeddingGenerator<string, Embedding<float>> embeddingGenerator) : IProductEmbeddingService
{
    /// <summary>
    ///     Generates embeddings for all existing products in batches and stores them in the database.
    /// </summary>
    /// <param name="cancellationToken">Token to cancel the operation.</param>
    public async Task<ErrorOr<Created>> GenerateVectorEmbeddingWithExistDataAsync(CancellationToken cancellationToken)
    {
        const int pageSize = 10;
        var query = productRepository.GetProductsInformation();
        query = query.OrderBy(x => x.Id);
        while (true)
        {
            var productList =
                await query.Take(pageSize).ToListAsync(cancellationToken);
            if (productList.Count == 0) break;
            var productEmbeddingList = new List<ProductEmbedding>();
            var textChunks = new List<string>();
            foreach (var product in productList)
            {
                var textChunk = GenerateText(product);
                textChunks.Add(textChunk);
            }

            var embeddingVectorResult = await GenerateVectorEmbeddingAsync(textChunks, cancellationToken);
            if (embeddingVectorResult.IsError) return embeddingVectorResult.Errors;

            var embeddingVector = embeddingVectorResult.Value;
            for (var index = 0; index < productList.Count; index++)
            {
                var newProductEmbedding = new ProductEmbedding
                {
                    ProductId = productList[index].Id,
                    TextChunk = textChunks[index],
                    Embedding = embeddingVector[index]
                };
                productEmbeddingList.Add(newProductEmbedding);
            }

            productEmbeddingRepository.AddRange(productEmbeddingList);
            await unitOfWork.SaveChangesAsync(cancellationToken);
        }

        return Result.Created;
    }

    /// <summary>
    ///     Generates and stores the embedding for a single product identified by its public ID.
    /// </summary>
    /// <param name="productId">Public product ID used to locate the product.</param>
    /// <param name="cancellationToken">Token to cancel the operation.</param>
    /// <returns>Created result on success or an error when the product is not found.</returns>
    public async Task<ErrorOr<Created>> GenerateVectorEmbeddingByProductPublicId(Guid productId,
        CancellationToken cancellationToken)
    {
        var product = await productRepository.GetProductInformationByPublicIdAsync(productId, cancellationToken);
        if (product is null)
            return Error.NotFound("Product.NotEmbeddable",
                "Product not found, deleted, or has no active selling variants.");

        var upsertResult = await UpsertProductEmbeddingAsync(product, cancellationToken);
        if (upsertResult.IsError) return upsertResult.Errors;

        return Result.Created;
    }

    /// <summary>
    ///     Synchronizes a product embedding with current product data, removing stale embeddings for non-embeddable products.
    /// </summary>
    /// <param name="productId">Public product ID used to locate the product.</param>
    /// <param name="cancellationToken">Token to cancel the operation.</param>
    /// <returns>Success when the embedding is upserted, removed, or already absent.</returns>
    public async Task<ErrorOr<Success>> SyncVectorEmbeddingByProductPublicId(Guid productId,
        CancellationToken cancellationToken)
    {
        var product = await productRepository.GetProductInformationByPublicIdAsync(productId, cancellationToken);
        if (product is null)
        {
            var existingEmbedding =
                await productEmbeddingRepository.GetByProductPublicIdAsync(productId, cancellationToken);
            if (existingEmbedding is not null)
            {
                productEmbeddingRepository.Delete(existingEmbedding);
                await unitOfWork.SaveChangesAsync(cancellationToken);
            }

            return Result.Success;
        }

        return await UpsertProductEmbeddingAsync(product, cancellationToken);
    }

    private async Task<ErrorOr<Success>> UpsertProductEmbeddingAsync(Product product, CancellationToken cancellationToken)
    {
        var textChunk = GenerateText(product);
        var vectorEmbeddingResult = await GenerateVectorEmbeddingAsync([textChunk], cancellationToken);
        if (vectorEmbeddingResult.IsError) return vectorEmbeddingResult.Errors;

        var existingEmbedding = await productEmbeddingRepository.GetByProductIdAsync(product.Id, cancellationToken);
        if (existingEmbedding is null)
        {
            productEmbeddingRepository.Add(new ProductEmbedding
            {
                ProductId = product.Id,
                TextChunk = textChunk,
                Embedding = vectorEmbeddingResult.Value[0]
            });
        }
        else
        {
            existingEmbedding.TextChunk = textChunk;
            existingEmbedding.Embedding = vectorEmbeddingResult.Value[0];
            productEmbeddingRepository.Update(existingEmbedding);
        }

        await unitOfWork.SaveChangesAsync(cancellationToken);
        return Result.Success;
    }

    /// <summary>
    ///     Builds a text representation of a product for embedding generation.
    /// </summary>
    /// <param name="product">Product with variants, category, and brand.</param>
    /// <returns>Flattened text describing the product and its variants.</returns>
    private static string GenerateText(Product product)
    {
        var listVariantInfo = product.ProductVariants
            .Where(p => p.IsSelling && !p.IsDeleted)
            .GroupBy(p => new { ColorName = p.Color?.ColorName ?? "Unknown", p.Price })
            .Select(pr => new
            {
                pr.Key.ColorName,
                pr.Key.Price,
                Size = pr.Select(x => x.Size?.Size.ToString() ?? "Unknown").ToList()
            }).ToList();
        var infoTextBuilder =
            new StringBuilder(
                $"Product: {product.ProductName}. Description: {product.Description ?? "No description"}. Brand: {product.Brand ?? "Unknown"}. Category: {product.Category?.Name ?? "Unknown"}. ");
        foreach (var variant in listVariantInfo)
            infoTextBuilder.Append(
                $"Variant: Available colors: {variant.ColorName}, Price: {variant.Price}, Available sizes: {string.Join(", ", variant.Size)}. ");
        return infoTextBuilder.ToString();
    }

    /// <summary>
    ///     Generates vector embeddings for the provided text chunks.
    /// </summary>
    /// <param name="textChunk">Ordered list of text chunks to embed.</param>
    /// <param name="token">Token to cancel the operation.</param>
    /// <returns>Embedding vectors aligned with the input order.</returns>
    private async Task<ErrorOr<List<ReadOnlyMemory<float>>>> GenerateVectorEmbeddingAsync(List<string> textChunk,
        CancellationToken token)
    {
        var vector = await embeddingGenerator.GenerateAsync(textChunk, cancellationToken: token);
        var result = vector.Select(v => v.Vector).ToList();

        if (result.Count != textChunk.Count)
            return Error.Failure("ProductEmbedding.VectorCountMismatch",
                $"Expected {textChunk.Count} embedding vectors but received {result.Count}.");

        for (var index = 0; index < result.Count; index++)
            if (result[index].Length != ProductEmbedding.EmbeddingDimensions)
                return Error.Failure("ProductEmbedding.InvalidVectorDimension",
                    $"Expected embedding dimension {ProductEmbedding.EmbeddingDimensions} but received {result[index].Length} at index {index}.");

        return result;
    }
}
