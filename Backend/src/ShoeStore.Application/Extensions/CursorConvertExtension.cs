using System.Globalization;
using System.Text;
using ShoeStore.Application.Constants;

namespace ShoeStore.Application.Extensions;

public static class CursorConvertExtension
{
    public static string? ConvertToBase64(this string? cursor)
    {
        if (string.IsNullOrEmpty(cursor))
            return null;

        var cursorBase64 = Convert.ToBase64String(Encoding.UTF8.GetBytes(cursor));
        return cursorBase64;
    }

    public static Cursor? ConvertToCursor(this string? cursor)
    {
        if (string.IsNullOrEmpty(cursor))
            return null;
        try
        {
            var decode = Convert.FromBase64String(cursor);
            var decodeString = Encoding.UTF8.GetString(decode);
            var parts = decodeString.Split('_');
            if (parts.Length == 2 &&
                DateTime.TryParse(parts[0], null, DateTimeStyles.RoundtripKind, out var dateTime) &&
                Guid.TryParse(parts[1], out var publicId))
                return new Cursor(publicId, dateTime);
        }
        catch (FormatException)
        {
        }

        return null;
    }
}