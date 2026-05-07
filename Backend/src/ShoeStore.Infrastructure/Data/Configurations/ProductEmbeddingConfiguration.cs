using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities.Embedding;
using Pgvector;

namespace ShoeStore.Infrastructure.Data.Configurations;

public class ProductEmbeddingConfiguration : IEntityTypeConfiguration<ProductEmbedding>
{
    public void Configure(EntityTypeBuilder<ProductEmbedding> builder)
    {
        builder.HasKey(e => e.Id);

        builder.HasOne(e => e.Product)
            .WithMany()
            .HasForeignKey(e => e.ProductId)
            .OnDelete(DeleteBehavior.Cascade);

        // set vector dimension = 768 for Gemini
        builder.Property(e => e.Embedding)
            .HasColumnType("vector(768)")
            .HasConversion( 
                v => new Vector(v.ToArray()), // convert readonlymemory to vector when writing to database
                v => new ReadOnlyMemory<float>(v.ToArray())// convert vector to readonlymemory when reading from database
            )
            .IsRequired();

        // force PostgreSQL uses HNSW index for the embedding column with cosine similarity algorithm operator
        builder.HasIndex(e => e.Embedding)
            .HasMethod("hnsw")
            .HasOperators("vector_cosine_ops");
    }
}