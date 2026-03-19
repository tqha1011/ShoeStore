using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ShoeStore.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class DbSeedData2 : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.UpdateData(
                table: "users",
                keyColumn: "id",
                keyValue: 1,
                columns: new[] { "created_at", "user_name" },
                values: new object[] { new DateTime(2026, 3, 19, 13, 44, 2, 320, DateTimeKind.Utc).AddTicks(9019), "admin1" });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.UpdateData(
                table: "users",
                keyColumn: "id",
                keyValue: 1,
                columns: new[] { "created_at", "user_name" },
                values: new object[] { new DateTime(2026, 3, 19, 13, 5, 34, 64, DateTimeKind.Utc).AddTicks(7323), "admin" });
        }
    }
}
