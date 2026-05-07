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
        var totalCount = await query.CountAsync(cancellationToken);
        var totalPages = (int)Math.Ceiling(totalCount / (double)pageSize);
        for (var pageIndex = 1; pageIndex <= totalPages; pageIndex++)
        {
            var productList =
                await query.Skip((pageIndex - 1) * pageSize).Take(pageSize).ToListAsync(cancellationToken);
            var productEmbeddingList = new List<ProductEmbedding>();
            var textChunks = new List<string>();
            foreach (var product in productList)
            {
                var textChunk = GenerateText(product);
                textChunks.Add(textChunk);
            }

            var embeddingVector = await GenerateVectorEmbeddingAsync(textChunks, cancellationToken);
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
        var product = await productRepository.GetDetailsByGuidAsync(productId, cancellationToken);
        if (product is null) return Error.NotFound("Product.NotFound", "Product not found");
        var textChunk = GenerateText(product);
        var textChunks = new List<string> { textChunk };
        var vectorEmbedding = await GenerateVectorEmbeddingAsync(textChunks, cancellationToken);
        var newProductEmbedding = new ProductEmbedding
        {
            ProductId = product.Id,
            TextChunk = textChunk,
            Embedding = vectorEmbedding[0]
        };
        productEmbeddingRepository.Add(newProductEmbedding);
        await unitOfWork.SaveChangesAsync(cancellationToken);
        return Result.Created;
    }

    /// <summary>
    ///     Builds a text representation of a product for embedding generation.
    /// </summary>
    /// <param name="product">Product with variants, category, and brand.</param>
    /// <returns>Flattened text describing the product and its variants.</returns>
    private static string GenerateText(Product product)
    {
        var listVariantInfo = product.ProductVariants.GroupBy(p => new { p.Color!.ColorName, p.Price })
            .Select(pr => new
            {
                pr.Key.ColorName,
                pr.Key.Price,
                Size = pr.Select(x => x.Size!.Size).ToList()
            }).ToList();
        var infoTextBuilder =
            new StringBuilder(
                $"Product: {product.ProductName}. Brand: {product.Brand}. Category: {product.Category!.Name}. ");
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
    private async Task<List<ReadOnlyMemory<float>>> GenerateVectorEmbeddingAsync(List<string> textChunk,
        CancellationToken token)
    {
        var vector = await embeddingGenerator.GenerateAsync(textChunk, cancellationToken: token);
        var result = vector.Select(v => v.Vector).ToList();
        return result;
    }
}