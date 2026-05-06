using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities.Embedding;

public class ProductVariantEmbedding : Entity<int>
{
    public int VariantId { get; set; }

    public ProductVariant? ProductVariant { get; set; }

    public required string TextChunk { get; set; }

    public ReadOnlyMemory<float> Embedding { get; set; } // vector database
}