namespace ShoeStore.Application.Utilities;

public static class CalculateShippingFee
{
    public static decimal CalculateShip(this string? shippingAddress)
    {
        if (string.IsNullOrWhiteSpace(shippingAddress)) return 40000; // 40k VND for empty address

        var addressLower = shippingAddress.ToLower();
        if (addressLower.Contains("Hồ Chí Minh", StringComparison.OrdinalIgnoreCase) ||
            addressLower.Contains("HCM", StringComparison.OrdinalIgnoreCase) ||
            addressLower.Contains("TPHCM", StringComparison.OrdinalIgnoreCase))
            return 20000; // 20k VND for Ho Chi Minh City

        return 35000; // 35k VND for other provinces
    }
}