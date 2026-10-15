using System.Collections.Concurrent;
using ShoeStore.Application.Utilities;

namespace ShoeStore.Tests.Unit.Utilities;

public class GenerateOrderCodeTests
{
    [Fact]
    public void Generate_KeepsPrefixAndDate_WithSixteenRandomDigits()
    {
        var code = GenerateOrderCode.Generate("DH");
        Assert.Matches(@"^DH\d{6}\d{16}$", code);
    }

    [Fact]
    public void Generate_ConcurrentCheckoutBursts_DoNotReuseOrderCodes()
    {
        const int count = 20_000;
        var codes = new ConcurrentBag<string>();
        Parallel.For(0, count, _ => codes.Add(GenerateOrderCode.Generate("DH")));
        Assert.Equal(count, codes.Distinct().Count());
    }
}
