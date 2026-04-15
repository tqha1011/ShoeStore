using TimeZoneConverter;

namespace ShoeStore.Application.Extensions;

public static class DateTimeExtensions
{
    extension(DateTime utcDateTime)
    {
        public DateTime ToVnTime()
        {
            if (utcDateTime.Kind == DateTimeKind.Local) utcDateTime = utcDateTime.ToUniversalTime();

            var vnTimeZone = TZConvert.GetTimeZoneInfo("Asia/Ho_Chi_Minh");

            return TimeZoneInfo.ConvertTimeFromUtc(utcDateTime, vnTimeZone);
        }

        public DateTime ToFirstDayOfMonth()
        {
            return new DateTime(utcDateTime.Year, utcDateTime.Month, 1, 0, 0, 0);
        }
    }
}