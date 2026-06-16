using Microsoft.EntityFrameworkCore.Migrations;
using Pgvector;

#nullable disable

namespace ShoeStore.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class FixProductEmbeddingUpsertAndDimension : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.Sql("DELETE FROM product_embeddings;");

            migrationBuilder.DropIndex(
                name: "ix_product_embeddings_product_id",
                table: "product_embeddings");

            migrationBuilder.AlterColumn<Vector>(
                name: "embedding",
                table: "product_embeddings",
                type: "vector(768)",
                nullable: false,
                oldClrType: typeof(Vector),
                oldType: "vector(1536)");

            migrationBuilder.CreateIndex(
                name: "ix_product_embeddings_product_id",
                table: "product_embeddings",
                column: "product_id",
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "ix_product_embeddings_product_id",
                table: "product_embeddings");

            migrationBuilder.AlterColumn<Vector>(
                name: "embedding",
                table: "product_embeddings",
                type: "vector(1536)",
                nullable: false,
                oldClrType: typeof(Vector),
                oldType: "vector(768)");

            migrationBuilder.CreateIndex(
                name: "ix_product_embeddings_product_id",
                table: "product_embeddings",
                column: "product_id");
        }
    }
}
