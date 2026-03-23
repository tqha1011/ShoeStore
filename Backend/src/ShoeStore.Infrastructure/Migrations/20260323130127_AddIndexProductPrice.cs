using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ShoeStore.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AddIndexProductPrice : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "ix_product_variants_is_deleted_is_selling",
                table: "product_variants");

            migrationBuilder.CreateIndex(
                name: "ix_product_variants_is_deleted_is_selling_price",
                table: "product_variants",
                columns: new[] { "is_deleted", "is_selling", "price" });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "ix_product_variants_is_deleted_is_selling_price",
                table: "product_variants");

            migrationBuilder.CreateIndex(
                name: "ix_product_variants_is_deleted_is_selling",
                table: "product_variants",
                columns: new[] { "is_deleted", "is_selling" });
        }
    }
}
