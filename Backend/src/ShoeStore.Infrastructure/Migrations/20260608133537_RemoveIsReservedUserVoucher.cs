using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ShoeStore.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class RemoveIsReservedUserVoucher : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<int>(
                name: "voucher_id",
                table: "voucher_details",
                type: "integer",
                nullable: false,
                defaultValue: 0,
                oldClrType: typeof(int),
                oldType: "integer",
                oldNullable: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.AlterColumn<int>(
                name: "voucher_id",
                table: "voucher_details",
                type: "integer",
                nullable: true,
                oldClrType: typeof(int),
                oldType: "integer");
        }
    }
}
