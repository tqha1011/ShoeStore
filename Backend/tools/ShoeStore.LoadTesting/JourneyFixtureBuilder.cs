namespace ShoeStore.LoadTesting;

public sealed record JourneyProduct(string ProductId, string ProductName, string VariantId, int Stock);

public static class JourneyFixtureBuilder
{
    public static List<JourneyProduct> Select(IEnumerable<JourneyProduct> candidates, int users)
    {
        var selected = candidates.Where(p => p.Stock > 0).DistinctBy(p => p.VariantId).Take(users).ToList();
        if (selected.Count != users)
            throw new InvalidOperationException($"Journey needs {users} distinct in-stock variants; found {selected.Count}.");
        return selected;
    }
}
