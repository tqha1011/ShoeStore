using System;
using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace ShoeStore.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class DbSeedData : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.InsertData(
                table: "users",
                columns: new[] { "id", "address", "created_at", "date_of_birth", "email", "password", "role", "updated_at", "user_name" },
                values: new object[] { 1, null, new DateTime(2026, 3, 19, 13, 5, 34, 64, DateTimeKind.Utc).AddTicks(7323), null, "admin1@gmail.com", "$2a$20$OTMgqqRjT5H.eoJtIAWqvuGWjiVyq8L36wAYDUUS55hbiLDNkvV1K", 1, null, "admin" });
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DeleteData(
                table: "users",
                keyColumn: "id",
                keyValue: 1);
        }
    }
}
