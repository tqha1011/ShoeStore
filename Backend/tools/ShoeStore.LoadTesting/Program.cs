using System.Text.Json;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Npgsql;
using ShoeStore.Domain.Enum;
using ShoeStore.Infrastructure.Authentication;
using ShoeStore.Infrastructure.Data;
using ShoeStore.LoadTesting;

var jsonOptions = new JsonSerializerOptions { PropertyNamingPolicy = JsonNamingPolicy.CamelCase, WriteIndented = true };
var connectionString = Environment.GetEnvironmentVariable("ConnectionStrings__DefaultConnection")
    ?? "Host=127.0.0.1;Port=5433;Database=shoestore_loadtest;Username=shoestore;Password=shoestore_local_only;Maximum Pool Size=80";
var cs = new NpgsqlConnectionStringBuilder(connectionString);
// Never change the old Azure DB or another project's database.
if (cs.Database != "shoestore_loadtest" || cs.Host is not ("localhost" or "127.0.0.1" or "::1"))
    throw new InvalidOperationException("Load-test utility only accepts a loopback host and database shoestore_loadtest.");

var command = args.ElementAtOrDefault(0) ?? "help";
var output = Path.GetFullPath(args.ElementAtOrDefault(1) ?? ".artifacts/loadtest");
Directory.CreateDirectory(output);
await using var connection = new NpgsqlConnection(connectionString);
await connection.OpenAsync();

switch (command)
{
    case "seed":
    {
        var options = new DbContextOptionsBuilder<AppDbContext>()
            .UseNpgsql(connectionString, o => o.UseVector()).UseSnakeCaseNamingConvention().Options;
        await using var context = new AppDbContext(options);
        await context.Database.MigrateAsync();
        await Execute("CREATE TABLE IF NOT EXISTS loadtest_seed_metadata (version integer PRIMARY KEY, seeded_at timestamptz NOT NULL DEFAULT now())");
        if (await Scalar("SELECT count(*) FROM loadtest_seed_metadata WHERE version=1") == 0)
        {
            if (await Scalar("SELECT count(*) FROM products") != 0 || await Scalar("SELECT count(*) FROM payments") != 0)
                throw new InvalidOperationException("Database already has unrecognised data; refusing to seed over it.");
            await using var transaction = await connection.BeginTransactionAsync();
            await using var seed = new NpgsqlCommand(await File.ReadAllTextAsync(Path.Combine(AppContext.BaseDirectory, "seed.sql")), connection, transaction);
            seed.CommandTimeout = 180;
            seed.Parameters.AddWithValue("password_hash", new PasswordHasher().HashPassword(
                Environment.GetEnvironmentVariable("TEST_PASSWORD") ?? "LoadTest_Local_2026!"));
            await seed.ExecuteNonQueryAsync();
            await using var marker = new NpgsqlCommand("INSERT INTO loadtest_seed_metadata (version) VALUES (1)", connection, transaction);
            await marker.ExecuteNonQueryAsync();
            await transaction.CommitAsync();
            Console.WriteLine("Seed inserted. Re-running seed preserves existing data and stock.");
        }
        else Console.WriteLine("Seed already exists; no duplicate inserts or stock reset.");

        await Execute("ANALYZE");
        Console.WriteLine(JsonSerializer.Serialize(new
        {
            databaseMaxConnections = await Scalar("SELECT current_setting('max_connections')::bigint"),
            applicationPoolLimit = cs.MaxPoolSize,
            users = await Scalar("SELECT count(*) FROM users WHERE id BETWEEN 10000 AND 10999"),
            products = await Scalar("SELECT count(*) FROM products"),
            variants = await Scalar("SELECT count(*) FROM product_variants"),
            historicalOrders = await Scalar("SELECT count(*) FROM invoices WHERE order_code LIKE 'LTH-%'"),
            historicalLines = await Scalar("SELECT count(*) FROM invoice_details WHERE invoice_id BETWEEN 100000 AND 149999")
        }, jsonOptions));
        break;
    }
    case "prepare":
    {
        var mode = args.ElementAtOrDefault(2) ?? "last-pair";
        var users = int.Parse(args.ElementAtOrDefault(3) ?? "500");
        if (mode is not ("last-pair" or "hot-sku" or "mixed" or "journey") || users < 1 || users > 1000)
            throw new ArgumentException("Mode: last-pair|hot-sku|mixed|journey; users: 1..1000.");
        if (await Scalar("SELECT count(*) FROM loadtest_seed_metadata WHERE version=1") != 1)
            throw new InvalidOperationException("Run seed first.");
        if (mode is "last-pair" or "hot-sku")
            await Execute($"UPDATE product_variants SET stock={(mode == "last-pair" ? 1 : 100)} WHERE id=10000");

        var runId = "LT-" + DateTime.UtcNow.ToString("yyyyMMddHHmmss") + "-" + Guid.NewGuid().ToString("N")[..8];
        var config = new ConfigurationBuilder().AddInMemoryCollection(new Dictionary<string, string?>
        {
            ["Jwt:Key"] = Environment.GetEnvironmentVariable("Jwt__Key") ?? "ShoeStore_Local_LoadTest_Only_Key_2026_Change_For_Production_12345",
            ["Jwt:Issuer"] = Environment.GetEnvironmentVariable("Jwt__Issuer") ?? "shoestore-loadtest",
            ["Jwt:Audience"] = Environment.GetEnvironmentVariable("Jwt__Audience") ?? "shoestore-loadtest"
        }).Build();
        var tokenService = new TokenService(config);
        var tokens = new List<object>();
        await using (var query = new NpgsqlCommand("SELECT public_id,email FROM users WHERE id BETWEEN 10000 AND 10999 ORDER BY id LIMIT @count", connection))
        {
            query.Parameters.AddWithValue("count", users);
            await using var reader = await query.ExecuteReaderAsync();
            while (await reader.ReadAsync()) tokens.Add(new { email = reader.GetString(1), token = tokenService.GenerateToken(reader.GetGuid(0), reader.GetString(1), UserRole.User) });
        }
        if (tokens.Count != users) throw new InvalidOperationException("Not enough seeded users.");
        var stocks = new List<StockSnapshot>();
        await using (var query = new NpgsqlCommand("SELECT public_id,stock FROM product_variants WHERE id BETWEEN 10000 AND 17499 ORDER BY id", connection))
        await using (var reader = await query.ExecuteReaderAsync())
            while (await reader.ReadAsync()) stocks.Add(new(reader.GetGuid(0).ToString(), reader.GetInt32(1)));
        if (stocks.Count != 7500) throw new InvalidOperationException("Seeded catalogue is incomplete.");
        var baskets = Enumerable.Range(0, users).Select(index => new OrderBasket(10000 + index,
            mode == "mixed"
                ? [new(stocks[1 + index].VariantId, 1 + index % 3), new(stocks[1 + users + index].VariantId, 1)]
                : [new(stocks[0].VariantId, 1)])).ToList();
        if (mode == "journey")
        {
            var candidates = new List<JourneyProduct>();
            await using var query = new NpgsqlCommand("""
                SELECT p.public_id,p.product_name,v.public_id,v.stock
                FROM products p JOIN product_variants v ON v.product_id=p.id
                WHERE p.id BETWEEN 10000 AND 10499 AND NOT p.is_deleted
                AND v.id BETWEEN 10001 AND 17499 AND v.is_selling AND NOT v.is_deleted
                ORDER BY (v.id-10000)%15,p.id
                """, connection);
            await using (var reader = await query.ExecuteReaderAsync())
                while (await reader.ReadAsync()) candidates.Add(new(reader.GetGuid(0).ToString(), reader.GetString(1),
                    reader.GetGuid(2).ToString(), reader.GetInt32(3)));
            var journey = JourneyFixtureBuilder.Select(candidates, users);
            baskets = journey.Select((product, index) => new OrderBasket(10000 + index,
                [new(product.VariantId, 1)])).ToList();
            await Write("journey.json", journey);
        }
        await Write("users.json", tokens);
        await Write("variants.json", stocks.Select(s => s.VariantId).ToList());
        await Write("baskets.json", baskets);
        await Write("config.json", new { runId, mode, users, hotStock = mode == "last-pair" ? 1 : 100 });
        await Write("baseline.json", new Baseline(runId, mode, users, stocks, baskets));
        if (!OperatingSystem.IsWindows()) File.SetUnixFileMode(Path.Combine(output, "users.json"), UnixFileMode.UserRead | UnixFileMode.UserWrite);
        Console.WriteLine($"Prepared {mode}: {users} independent users, run {runId}. JWTs expire in 1 hour.");
        break;
    }
    case "verify":
    {
        var baseline = JsonSerializer.Deserialize<Baseline>(await File.ReadAllTextAsync(Path.Combine(output, "baseline.json")), jsonOptions)!;
        if (baseline.Baskets == null) throw new InvalidOperationException("Old baseline has no submitted baskets; prepare a new run.");
        using var summary = JsonDocument.Parse(await File.ReadAllTextAsync(Path.Combine(output, "summary.json")));
        var metrics = summary.RootElement.GetProperty("metrics");
        long MetricCount(string name) => metrics.TryGetProperty(name, out var metric)
            ? metric.GetProperty("values").GetProperty("count").GetInt64() : 0;
        var audits = new List<StockAudit>();
        var before = baseline.Stocks.ToDictionary(s => s.VariantId, s => s.Stock);
        await using (var query = new NpgsqlCommand("""
            SELECT v.public_id, v.stock, coalesce(sum(d.quantity) FILTER (WHERE i.full_name=@run),0)::bigint
            FROM product_variants v LEFT JOIN invoice_details d ON d.product_variant_id=v.id
            LEFT JOIN invoices i ON i.id=d.invoice_id
            WHERE v.id BETWEEN 10000 AND 17499 GROUP BY v.id,v.public_id,v.stock ORDER BY v.id
            """, connection))
        {
            query.Parameters.AddWithValue("run", baseline.RunId);
            await using var reader = await query.ExecuteReaderAsync();
            while (await reader.ReadAsync())
            {
                var id = reader.GetGuid(0).ToString();
                audits.Add(new(id, before[id], reader.GetInt32(1), reader.GetInt64(2)));
            }
        }
        var invoices = await Scalar("SELECT count(*) FROM invoices WHERE full_name=@run", baseline.RunId);
        var success = MetricCount("orders_succeeded");
        var failures = InventoryVerifier.FindFailures(audits, invoices, success,
            MetricCount("orders_attempted"), baseline.Users, MetricCount("orders_unexpected"));
        var savedBaskets = new Dictionary<int, OrderBasket>();
        await using (var query = new NpgsqlCommand("""
            SELECT i.id,i.user_id,v.public_id,d.quantity FROM invoices i
            JOIN invoice_details d ON d.invoice_id=i.id
            JOIN product_variants v ON v.id=d.product_variant_id
            WHERE i.full_name=@run ORDER BY i.id,d.id
            """, connection))
        {
            query.Parameters.AddWithValue("run", baseline.RunId);
            await using var reader = await query.ExecuteReaderAsync();
            while (await reader.ReadAsync())
            {
                var invoiceId = reader.GetInt32(0);
                if (!savedBaskets.TryGetValue(invoiceId, out var basket))
                    savedBaskets[invoiceId] = basket = new OrderBasket(reader.GetInt32(1), []);
                basket.Items.Add(new(reader.GetGuid(2).ToString(), reader.GetInt32(3)));
            }
        }
        var basketFailures = InventoryVerifier.FindBasketFailures(baseline.Baskets, savedBaskets.Values);
        failures.AddRange(basketFailures);
        if (audits.Count != baseline.Stocks.Count) failures.Add("Catalogue changed during test.");
        if (baseline.Mode == "last-pair" && invoices != 1) failures.Add("Last-pair test must commit exactly one invoice.");
        if (baseline.Mode == "hot-sku" && invoices > 100) failures.Add("Hot-SKU test sold more than 100 pairs.");
        if (baseline.Mode == "mixed" && invoices != baseline.Users) failures.Add("Mixed test expects all independent baskets to succeed.");
        if (baseline.Mode == "journey" && invoices != baseline.Users) failures.Add("Journey expects every customer to buy successfully.");
        var badInvoices = await Scalar("""
            SELECT count(*) FROM invoices i WHERE full_name=@run AND
            (i.payment_id<>2 OR i.status<>1 OR NOT EXISTS (SELECT 1 FROM invoice_details d WHERE d.invoice_id=i.id)
             OR i.final_price<>i.shipping_fee+(SELECT sum(d.quantity*d.unit_price) FROM invoice_details d WHERE d.invoice_id=i.id))
            """, baseline.RunId);
        if (badInvoices > 0) failures.Add($"Invalid/empty invoices: {badInvoices}");
        var detailCount = await Scalar("SELECT count(*) FROM invoice_details d JOIN invoices i ON i.id=d.invoice_id WHERE i.full_name=@run", baseline.RunId);
        if (detailCount != invoices * (baseline.Mode == "mixed" ? 2 : 1)) failures.Add("Invoice line count differs from submitted baskets.");
        var report = new
        {
            baseline.RunId, baseline.Mode, attempted = MetricCount("orders_attempted"), succeeded = success,
            conflicts = MetricCount("orders_conflict"), outOfStock = MetricCount("orders_out_of_stock"),
            stockChanged = MetricCount("orders_stock_changed"),
            unexpected = MetricCount("orders_unexpected"), committedInvoices = invoices,
            committedLines = detailCount, orderedQuantity = audits.Sum(a => a.OrderedQuantity),
            basketMismatches = basketFailures.Count,
            inventoryMismatches = audits.Count(a => a.Before != a.After + a.OrderedQuantity || a.After < 0),
            checkoutP95Ms = metrics.GetProperty("http_req_duration{phase:checkout}").GetProperty("values").GetProperty("p(95)").GetDouble(),
            passed = failures.Count == 0, failures
        };
        await Write("audit.json", report);
        Console.WriteLine(JsonSerializer.Serialize(report, jsonOptions));
        if (failures.Count > 0) Environment.ExitCode = 1;
        break;
    }
    default:
        Console.WriteLine("Commands: seed [output], prepare [output] [last-pair|hot-sku|mixed|journey] [users], verify [output]");
        break;
}

async Task Execute(string sql)
{
    await using var query = new NpgsqlCommand(sql, connection) { CommandTimeout = 180 };
    await query.ExecuteNonQueryAsync();
}
async Task<long> Scalar(string sql, string? runId = null)
{
    await using var query = new NpgsqlCommand(sql, connection) { CommandTimeout = 180 };
    if (runId != null) query.Parameters.AddWithValue("run", runId);
    return Convert.ToInt64(await query.ExecuteScalarAsync());
}
async Task Write(string name, object value) => await File.WriteAllTextAsync(Path.Combine(output, name), JsonSerializer.Serialize(value, jsonOptions));

public sealed record StockSnapshot(string VariantId, int Stock);
public sealed record Baseline(string RunId, string Mode, int Users, List<StockSnapshot> Stocks, List<OrderBasket> Baskets);
