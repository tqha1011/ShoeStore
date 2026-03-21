using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ShoeStore.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class AddGuid : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "ix_cart_items_user_id",
                table: "cart_items");

            migrationBuilder.AddColumn<Guid>(
                name: "public_id",
                table: "vouchers",
                type: "uuid",
                nullable: false,
                defaultValueSql: "gen_random_uuid()");

            migrationBuilder.AddColumn<Guid>(
                name: "public_id",
                table: "voucher_details",
                type: "uuid",
                nullable: false,
                defaultValueSql: "gen_random_uuid()");

            migrationBuilder.AddColumn<Guid>(
                name: "public_id",
                table: "users",
                type: "uuid",
                nullable: false,
                defaultValueSql: "gen_random_uuid()");

            migrationBuilder.AddColumn<Guid>(
                name: "public_id",
                table: "user_vouchers",
                type: "uuid",
                nullable: false,
                defaultValueSql: "gen_random_uuid()");

            migrationBuilder.AddColumn<Guid>(
                name: "public_id",
                table: "products",
                type: "uuid",
                nullable: false,
                defaultValueSql: "gen_random_uuid()");

            migrationBuilder.AlterColumn<int>(
                name: "color_id",
                table: "product_variants",
                type: "integer",
                nullable: true,
                oldClrType: typeof(int),
                oldType: "integer");

            migrationBuilder.AddColumn<Guid>(
                name: "public_id",
                table: "product_variants",
                type: "uuid",
                nullable: false,
                defaultValueSql: "gen_random_uuid()");

            migrationBuilder.AddColumn<Guid>(
                name: "public_id",
                table: "invoices",
                type: "uuid",
                nullable: false,
                defaultValueSql: "gen_random_uuid()");

            migrationBuilder.AddColumn<Guid>(
                name: "public_id",
                table: "invoice_details",
                type: "uuid",
                nullable: false,
                defaultValueSql: "gen_random_uuid()");

            migrationBuilder.AddColumn<Guid>(
                name: "public_id",
                table: "cart_items",
                type: "uuid",
                nullable: false,
                defaultValueSql: "gen_random_uuid()");

            migrationBuilder.UpdateData(
                table: "users",
                keyColumn: "id",
                keyValue: 1,
                column: "public_id",
                value: new Guid("1da60221-9f45-4ec3-a69a-3144b3520ebb"));

            migrationBuilder.CreateIndex(
                name: "ix_vouchers_public_id",
                table: "vouchers",
                column: "public_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "ix_voucher_details_public_id",
                table: "voucher_details",
                column: "public_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "ix_users_public_id",
                table: "users",
                column: "public_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "ix_user_vouchers_public_id",
                table: "user_vouchers",
                column: "public_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "ix_products_public_id",
                table: "products",
                column: "public_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "ix_product_variants_public_id",
                table: "product_variants",
                column: "public_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "ix_invoices_public_id",
                table: "invoices",
                column: "public_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "ix_invoice_details_public_id",
                table: "invoice_details",
                column: "public_id",
                unique: true);

            migrationBuilder.CreateIndex(
                name: "ix_cart_items_public_id",
                table: "cart_items",
                column: "public_id",
                unique: true);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropIndex(
                name: "ix_vouchers_public_id",
                table: "vouchers");

            migrationBuilder.DropIndex(
                name: "ix_voucher_details_public_id",
                table: "voucher_details");

            migrationBuilder.DropIndex(
                name: "ix_users_public_id",
                table: "users");

            migrationBuilder.DropIndex(
                name: "ix_user_vouchers_public_id",
                table: "user_vouchers");

            migrationBuilder.DropIndex(
                name: "ix_products_public_id",
                table: "products");

            migrationBuilder.DropIndex(
                name: "ix_product_variants_public_id",
                table: "product_variants");

            migrationBuilder.DropIndex(
                name: "ix_invoices_public_id",
                table: "invoices");

            migrationBuilder.DropIndex(
                name: "ix_invoice_details_public_id",
                table: "invoice_details");

            migrationBuilder.DropIndex(
                name: "ix_cart_items_public_id",
                table: "cart_items");

            migrationBuilder.DropColumn(
                name: "public_id",
                table: "vouchers");

            migrationBuilder.DropColumn(
                name: "public_id",
                table: "voucher_details");

            migrationBuilder.DropColumn(
                name: "public_id",
                table: "users");

            migrationBuilder.DropColumn(
                name: "public_id",
                table: "user_vouchers");

            migrationBuilder.DropColumn(
                name: "public_id",
                table: "products");

            migrationBuilder.DropColumn(
                name: "public_id",
                table: "product_variants");

            migrationBuilder.DropColumn(
                name: "public_id",
                table: "invoices");

            migrationBuilder.DropColumn(
                name: "public_id",
                table: "invoice_details");

            migrationBuilder.DropColumn(
                name: "public_id",
                table: "cart_items");

            migrationBuilder.AlterColumn<int>(
                name: "color_id",
                table: "product_variants",
                type: "integer",
                nullable: false,
                defaultValue: 0,
                oldClrType: typeof(int),
                oldType: "integer",
                oldNullable: true);

            migrationBuilder.CreateIndex(
                name: "ix_cart_items_user_id",
                table: "cart_items",
                column: "user_id");
        }
    }
}
