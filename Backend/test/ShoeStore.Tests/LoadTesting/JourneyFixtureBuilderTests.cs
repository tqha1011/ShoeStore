using ShoeStore.LoadTesting;

namespace ShoeStore.Tests.LoadTesting;

public class JourneyFixtureBuilderTests
{
    [Fact]
    public void Journey_UsesOnlyVariantsThatCanBeBought()
    {
        var selected = JourneyFixtureBuilder.Select([
            Product("sold-out", 0), Product("negative", -1), Product("available", 1)
        ], 1);
        Assert.Equal("available", Assert.Single(selected).VariantId);
    }

    [Fact]
    public void Journey_AssignsDistinctVariantsToIndependentBuyers()
    {
        var selected = JourneyFixtureBuilder.Select([
            Product("a", 1), Product("a", 1), Product("b", 1)
        ], 2);
        Assert.Equal(new[] { "a", "b" }, selected.Select(p => p.VariantId));
    }

    [Fact]
    public void Journey_RejectsInsufficientFixtureInsteadOfStartingBrokenLoadTest()
    {
        Assert.Throws<InvalidOperationException>(() =>
            JourneyFixtureBuilder.Select([Product("a", 1)], 500));
    }

    private static JourneyProduct Product(string variantId, int stock) =>
        new("product", "Test shoe", variantId, stock);
}
