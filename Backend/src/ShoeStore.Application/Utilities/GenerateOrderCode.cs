namespace ShoeStore.Application.Utilities;

public static class GenerateOrderCode
{
    /// <summary>
    ///     Generates a unique order code in the format: [Prefix][YYMMDD][RandomString]
    ///     Example: DH260407A1B2
    /// </summary>
    /// <param name="prefix"></param>
    /// <returns></returns>
    public static string Generate(string prefix)
    {
        var datePart = DateTime.Now.ToString("yyMMdd");

        const string chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        var random = new Random();

        var randomPart = new string(Enumerable.Repeat(chars, 4)
            .Select(s => s[random.Next(s.Length)]).ToArray());

        return $"{prefix}{datePart}{randomPart}";
    }
}