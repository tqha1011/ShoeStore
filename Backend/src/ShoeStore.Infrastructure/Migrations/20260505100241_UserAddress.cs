using System;
using Microsoft.EntityFrameworkCore.Migrations;
using Npgsql.EntityFrameworkCore.PostgreSQL.Metadata;

#nullable disable

namespace ShoeStore.Infrastructure.Migrations
{
    /// <inheritdoc />
    public partial class UserAddress : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "fk_chat_message_chat_session_session_id",
                table: "chat_message");

            migrationBuilder.DropForeignKey(
                name: "fk_chat_session_users_user_id",
                table: "chat_session");

            migrationBuilder.DropPrimaryKey(
                name: "pk_chat_session",
                table: "chat_session");

            migrationBuilder.DropPrimaryKey(
                name: "pk_chat_message",
                table: "chat_message");

            migrationBuilder.RenameTable(
                name: "chat_session",
                newName: "chat_sessions");

            migrationBuilder.RenameTable(
                name: "chat_message",
                newName: "chat_messages");

            migrationBuilder.RenameIndex(
                name: "ix_chat_session_user_id_is_active",
                table: "chat_sessions",
                newName: "ix_chat_sessions_user_id_is_active");

            migrationBuilder.RenameIndex(
                name: "ix_chat_session_public_id",
                table: "chat_sessions",
                newName: "ix_chat_sessions_public_id");

            migrationBuilder.RenameIndex(
                name: "ix_chat_message_session_id_created_at",
                table: "chat_messages",
                newName: "ix_chat_messages_session_id_created_at");

            migrationBuilder.RenameIndex(
                name: "ix_chat_message_public_id",
                table: "chat_messages",
                newName: "ix_chat_messages_public_id");

            migrationBuilder.AddPrimaryKey(
                name: "pk_chat_sessions",
                table: "chat_sessions",
                column: "id");

            migrationBuilder.AddPrimaryKey(
                name: "pk_chat_messages",
                table: "chat_messages",
                column: "id");

            migrationBuilder.CreateTable(
                name: "user_addresses",
                columns: table => new
                {
                    id = table.Column<int>(type: "integer", nullable: false)
                        .Annotation("Npgsql:ValueGenerationStrategy", NpgsqlValueGenerationStrategy.IdentityByDefaultColumn),
                    address = table.Column<string>(type: "character varying(500)", maxLength: 500, nullable: false),
                    created_at = table.Column<DateTime>(type: "timestamp with time zone", nullable: false),
                    user_id = table.Column<int>(type: "integer", nullable: false),
                    is_default = table.Column<bool>(type: "boolean", nullable: false)
                },
                constraints: table =>
                {
                    table.PrimaryKey("pk_user_addresses", x => x.id);
                    table.ForeignKey(
                        name: "fk_user_addresses_users_user_id",
                        column: x => x.user_id,
                        principalTable: "users",
                        principalColumn: "id",
                        onDelete: ReferentialAction.Cascade);
                });

            migrationBuilder.CreateIndex(
                name: "ix_user_addresses_user_id",
                table: "user_addresses",
                column: "user_id");

            migrationBuilder.AddForeignKey(
                name: "fk_chat_messages_chat_sessions_session_id",
                table: "chat_messages",
                column: "session_id",
                principalTable: "chat_sessions",
                principalColumn: "id",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "fk_chat_sessions_users_user_id",
                table: "chat_sessions",
                column: "user_id",
                principalTable: "users",
                principalColumn: "id",
                onDelete: ReferentialAction.Cascade);
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropForeignKey(
                name: "fk_chat_messages_chat_sessions_session_id",
                table: "chat_messages");

            migrationBuilder.DropForeignKey(
                name: "fk_chat_sessions_users_user_id",
                table: "chat_sessions");

            migrationBuilder.DropTable(
                name: "user_addresses");

            migrationBuilder.DropPrimaryKey(
                name: "pk_chat_sessions",
                table: "chat_sessions");

            migrationBuilder.DropPrimaryKey(
                name: "pk_chat_messages",
                table: "chat_messages");

            migrationBuilder.RenameTable(
                name: "chat_sessions",
                newName: "chat_session");

            migrationBuilder.RenameTable(
                name: "chat_messages",
                newName: "chat_message");

            migrationBuilder.RenameIndex(
                name: "ix_chat_sessions_user_id_is_active",
                table: "chat_session",
                newName: "ix_chat_session_user_id_is_active");

            migrationBuilder.RenameIndex(
                name: "ix_chat_sessions_public_id",
                table: "chat_session",
                newName: "ix_chat_session_public_id");

            migrationBuilder.RenameIndex(
                name: "ix_chat_messages_session_id_created_at",
                table: "chat_message",
                newName: "ix_chat_message_session_id_created_at");

            migrationBuilder.RenameIndex(
                name: "ix_chat_messages_public_id",
                table: "chat_message",
                newName: "ix_chat_message_public_id");

            migrationBuilder.AddPrimaryKey(
                name: "pk_chat_session",
                table: "chat_session",
                column: "id");

            migrationBuilder.AddPrimaryKey(
                name: "pk_chat_message",
                table: "chat_message",
                column: "id");

            migrationBuilder.AddForeignKey(
                name: "fk_chat_message_chat_session_session_id",
                table: "chat_message",
                column: "session_id",
                principalTable: "chat_session",
                principalColumn: "id",
                onDelete: ReferentialAction.Cascade);

            migrationBuilder.AddForeignKey(
                name: "fk_chat_session_users_user_id",
                table: "chat_session",
                column: "user_id",
                principalTable: "users",
                principalColumn: "id",
                onDelete: ReferentialAction.Cascade);
        }
    }
}
