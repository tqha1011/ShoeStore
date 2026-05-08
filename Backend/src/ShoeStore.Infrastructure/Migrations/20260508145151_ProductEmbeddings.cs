using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ShoeStore.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class ProductEmbeddings : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "fk_product_embedding_products_product_id",
                table: "product_embedding");

            migrationBuilder.DropPrimaryKey(
                name: "pk_product_embedding",
                table: "product_embedding");

            migrationBuilder.RenameTable(
                name: "product_embedding",
                newName: "product_embeddings");

            migrationBuilder.RenameIndex(
                name: "ix_product_embedding_product_id",
                table: "product_embeddings",
                newName: "ix_product_embeddings_product_id");

            migrationBuilder.RenameIndex(
                name: "ix_product_embedding_embedding",
                table: "product_embeddings",
                newName: "ix_product_embeddings_embedding");

            migrationBuilder.AddPrimaryKey(
                name: "pk_product_embeddings",
                table: "product_embeddings",
                column: "id");

            migrationBuilder.AddForeignKey(
                name: "fk_product_embeddings_products_product_id",
                table: "product_embeddings",
                column: "product_id",
                principalTable: "products",
                principalColumn: "id",
                onDelete: ReferentialAction.Cascade);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "fk_product_embeddings_products_product_id",
                table: "product_embeddings");

            migrationBuilder.DropPrimaryKey(
                name: "pk_product_embeddings",
                table: "product_embeddings");

            migrationBuilder.RenameTable(
                name: "product_embeddings",
                newName: "product_embedding");

            migrationBuilder.RenameIndex(
                name: "ix_product_embeddings_product_id",
                table: "product_embedding",
                newName: "ix_product_embedding_product_id");

            migrationBuilder.RenameIndex(
                name: "ix_product_embeddings_embedding",
                table: "product_embedding",
                newName: "ix_product_embedding_embedding");

            migrationBuilder.AddPrimaryKey(
                name: "pk_product_embedding",
                table: "product_embedding",
                column: "id");

            migrationBuilder.AddForeignKey(
                name: "fk_product_embedding_products_product_id",
                table: "product_embedding",
                column: "product_id",
                principalTable: "products",
                principalColumn: "id",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
