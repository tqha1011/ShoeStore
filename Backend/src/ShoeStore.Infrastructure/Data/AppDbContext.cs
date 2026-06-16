using Microsoft.EntityFrameworkCore;
using Pgvector;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Entities.Embedding;

namespace ShoeStore.Infrastructure.Data;

public class AppDbContext(DbContextOptions<AppDbContext> options) : DbContext(options)
{
    public DbSet<Color> Colors { get; set; }
    public DbSet<Product> Products { get; set; }
    public DbSet<ProductVariant> ProductVariants { get; set; }
    public DbSet<Voucher> Vouchers { get; set; }
    public DbSet<VoucherDetail> VoucherDetails { get; set; }
    public DbSet<Invoice> Invoices { get; set; }
    public DbSet<InvoiceDetail> InvoiceDetails { get; set; }
    public DbSet<ProductSize> ProductSizes { get; set; }
    public DbSet<User> Users { get; set; }
    public DbSet<Payment> Payments { get; set; }
    public DbSet<CartItem> CartItems { get; set; }
    public DbSet<UserVoucher> UserVouchers { get; set; }
    public DbSet<UserRefreshToken> UserRefreshTokens { get; set; }
    public DbSet<UserRestorePassword> UserRestorePasswords { get; set; }
    public DbSet<PaymentTransaction> PaymentTransactions { get; set; }
    public DbSet<ChatSession> ChatSessions { get; set; }
    public DbSet<ChatMessage> ChatMessages { get; set; }
    public DbSet<UserAddress> UserAddresses { get; set; }
    public DbSet<ProductEmbedding> ProductEmbeddings { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        var providerName = Database.ProviderName ?? string.Empty;
        var isNpgsql = providerName.Contains("Npgsql", StringComparison.OrdinalIgnoreCase);
        var isSqlite = providerName.Contains("Sqlite", StringComparison.OrdinalIgnoreCase);

        if (isNpgsql)
        {
            modelBuilder.HasPostgresExtension("vector");
        }

        modelBuilder.ApplyConfigurationsFromAssembly(typeof(AppDbContext).Assembly);

        if (isNpgsql)
        {
            modelBuilder.Entity<ProductEmbedding>(builder =>
            {
                builder.Property(e => e.Embedding)
                    .HasColumnType($"vector({ProductEmbedding.EmbeddingDimensions})")
                    .HasConversion(
                        v => new Vector(v.ToArray()),
                        v => new ReadOnlyMemory<float>(v.ToArray()))
                    .IsRequired();

                builder.HasIndex(e => e.Embedding)
                    .HasMethod("hnsw")
                    .HasOperators("vector_cosine_ops");
            });

            modelBuilder.Entity<UserAddress>()
                .HasIndex(addr => addr.UserId)
                .IsUnique()
                .HasFilter("is_default = true");
        }
        else if (isSqlite)
        {
            // SQLite doesn't support pgvector; ignore for in-memory/testing provider.
            modelBuilder.Entity<ProductEmbedding>().Ignore(e => e.Embedding);
            // SQLite doesn't support rowversion; ignore shadow concurrency tokens in tests.
            modelBuilder.Entity<ProductVariant>().Ignore("Version");
            modelBuilder.Entity<Voucher>().Ignore("Version");
        }
    }
}
