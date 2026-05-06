using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;
using ShoeStore.Domain.Entities.Embedding;

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
            .HasColumnType("vector(768")
            .IsRequired();
        
        // force PostgreSQL uses HNSW index for the embedding column with cosine similarity algorithm operator
        builder.HasIndex(e => e.Embedding).HasMethod("hnsw").HasOperators("vector_cosine_ops");
    }
}