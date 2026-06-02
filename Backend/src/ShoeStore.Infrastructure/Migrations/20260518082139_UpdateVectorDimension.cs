using Microsoft.EntityFrameworkCore.Migrations;
using Pgvector;

#nullable disable

namespace ShoeStore.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class UpdateVectorDimension : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<Vector>(
                name: "embedding",
                table: "product_embeddings",
                type: "vector(1536)",
                nullable: false,
                oldClrType: typeof(Vector),
                oldType: "vector(768)");

            migrationBuilder.AlterColumn<string>(
                name: "title",
                table: "chat_sessions",
                type: "text",
                nullable: false,
                defaultValue: "",
                oldClrType: typeof(string),
                oldType: "text",
                oldNullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<Vector>(
                name: "embedding",
                table: "product_embeddings",
                type: "vector(768)",
                nullable: false,
                oldClrType: typeof(Vector),
                oldType: "vector(1536)");

            migrationBuilder.AlterColumn<string>(
                name: "title",
                table: "chat_sessions",
                type: "text",
                nullable: true,
                oldClrType: typeof(string),
                oldType: "text");
        }
    }
}
