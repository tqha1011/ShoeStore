using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ShoeStore.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AddMaxPriceDiscountToVoucher : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AddColumn<decimal>(
                name: "max_price_discount",
                table: "vouchers",
                type: "numeric(18,2)",
                nullable: false,
                defaultValue: 0m);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "max_price_discount",
                table: "vouchers");
        }
    }
}
