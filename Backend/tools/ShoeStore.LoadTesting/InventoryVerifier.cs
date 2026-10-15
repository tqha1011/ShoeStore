namespace ShoeStore.LoadTesting;

public sealed record StockAudit(string VariantId, int Before, int After, long OrderedQuantity);
public sealed record OrderLine(string VariantId, int Quantity);
public sealed record OrderBasket(int UserId, List<OrderLine> Items);

public static class InventoryVerifier
{
    public static List<string> FindBasketFailures(IEnumerable<OrderBasket> expected, IEnumerable<OrderBasket> committed)
    {
        var byUser = expected.ToDictionary(b => b.UserId);
        var seen = new HashSet<int>();
        var failures = new List<string>();
        foreach (var basket in committed)
        {
            if (!seen.Add(basket.UserId)) failures.Add($"Duplicate committed order for customer {basket.UserId}");
            if (!byUser.TryGetValue(basket.UserId, out var submitted) ||
                !basket.Items.OrderBy(i => i.VariantId).SequenceEqual(submitted.Items.OrderBy(i => i.VariantId)))
                failures.Add($"Saved basket differs from submitted variants/quantities for customer {basket.UserId}");
        }
        return failures;
    }

    public static List<string> FindFailures(IEnumerable<StockAudit> stocks, long invoices,
        long successfulResponses, long attempts, long expectedAttempts, long unexpectedResponses)
    {
        var failures = new List<string>();
        foreach (var stock in stocks)
        {
            if (stock.After < 0 || stock.OrderedQuantity < 0 ||
                stock.Before != stock.After + stock.OrderedQuantity)
                failures.Add($"Inventory mismatch for {stock.VariantId}: before={stock.Before}, " +
                    $"after={stock.After}, ordered={stock.OrderedQuantity}");
        }
        if (invoices != successfulResponses)
            failures.Add($"Committed invoices ({invoices}) != successful responses ({successfulResponses})");
        if (successfulResponses == 0)
            failures.Add("No successful orders: the test did not exercise a working checkout");
        if (attempts != expectedAttempts)
            failures.Add($"Attempted {attempts} orders, expected {expectedAttempts}");
        if (unexpectedResponses != 0)
            failures.Add($"Unexpected responses: {unexpectedResponses} (including 401/404/429/500)");
        return failures;
    }
}
