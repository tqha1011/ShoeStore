using System.Globalization;
using System.Security.Cryptography;

namespace ShoeStore.Application.Utilities;

public static class GenerateOrderCode
{
    /// <summary>
    ///     Generates an order code in the format: [Prefix][YYMMDD][16 random digits].
    ///     Example: DH2604071234567890123456. The database enforces uniqueness.
    /// </summary>
    /// <param name="prefix"></param>
    /// <returns></returns>
    public static string Generate(string prefix)
    {
        var datePart = DateTime.Now.ToString("yyMMdd", CultureInfo.InvariantCulture);

        // A large suffix avoids collisions across accumulated orders and checkout retries.
        // Digits preserve compatibility with PaymentService's DH\d+ content parser.
        const string chars = "0123456789";

        var randomPart = new string(Enumerable.Range(0, 16)
            .Select(_ => chars[RandomNumberGenerator.GetInt32(chars.Length)]).ToArray());

        return $"{prefix}{datePart}{randomPart}";
    }
}
