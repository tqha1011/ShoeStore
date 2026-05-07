using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities.Embedding;

public class ProductEmbedding : Entity<int>
{
    public required int ProductId { get; set; }

    public Product? Product { get; set; }

    public required string TextChunk { get; set; }

    public ReadOnlyMemory<float> Embedding { get; set; }  // vector database
}