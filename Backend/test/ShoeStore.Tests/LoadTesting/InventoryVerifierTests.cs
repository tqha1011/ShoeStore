using ShoeStore.LoadTesting;

namespace ShoeStore.Tests.LoadTesting;

public class InventoryVerifierTests
{
    [Fact]
    public void ConsistentlyDeductedButWrongQuantity_FailsBasketAudit()
    {
        Assert.NotEmpty(InventoryVerifier.FindBasketFailures(
            [new OrderBasket(10000, [new OrderLine("a", 3), new OrderLine("b", 1)])],
            [new OrderBasket(10000, [new OrderLine("a", 1), new OrderLine("b", 1)])]));
    }

    [Fact]
    public void WrongVariantOrDuplicateBuyer_FailsBasketAudit()
    {
        Assert.NotEmpty(InventoryVerifier.FindBasketFailures(
            [new OrderBasket(10000, [new OrderLine("a", 1)])],
            [new OrderBasket(10000, [new OrderLine("b", 1)])]));
        Assert.NotEmpty(InventoryVerifier.FindBasketFailures(
            [new OrderBasket(10000, [new OrderLine("a", 1)])],
            [new OrderBasket(10000, [new OrderLine("a", 1)]), new OrderBasket(10000, [new OrderLine("a", 1)])]));
    }

    [Fact]
    public void CorrectBasketWithReorderedLines_PassesBasketAudit()
    {
        Assert.Empty(InventoryVerifier.FindBasketFailures(
            [new OrderBasket(10000, [new OrderLine("a", 3), new OrderLine("b", 1)])],
            [new OrderBasket(10000, [new OrderLine("b", 1), new OrderLine("a", 3)])]));
    }

    [Fact]
    public void LostUpdate_WithNonNegativeStock_StillFailsAudit()
    {
        var failures = InventoryVerifier.FindFailures(
            [new StockAudit("variant", 1, 0, 2)], 2, 2, 500, 500, 0);
        Assert.Contains(failures, f => f.Contains("inventory", StringComparison.OrdinalIgnoreCase));
    }

    [Fact]
    public void OneWinnerAnd499ExpectedRejections_PassesAudit()
    {
        Assert.Empty(InventoryVerifier.FindFailures(
            [new StockAudit("variant", 1, 0, 1)], 1, 1, 500, 500, 0));
    }

    [Fact]
    public void ResponseSuccessWithoutCommittedInvoice_FailsAudit()
    {
        Assert.NotEmpty(InventoryVerifier.FindFailures(
            [new StockAudit("variant", 1, 1, 0)], 0, 1, 500, 500, 0));
    }

    [Fact]
    public void RateLimitedOrIncompleteBurst_FailsAudit()
    {
        Assert.NotEmpty(InventoryVerifier.FindFailures(
            [new StockAudit("variant", 1, 0, 1)], 1, 1, 499, 500, 0));
        Assert.NotEmpty(InventoryVerifier.FindFailures(
            [new StockAudit("variant", 1, 0, 1)], 1, 1, 500, 500, 499));
    }
}
