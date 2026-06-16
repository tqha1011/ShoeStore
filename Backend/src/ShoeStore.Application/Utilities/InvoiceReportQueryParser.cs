using System.Text.Json;
using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Utilities;

public static class InvoiceReportQueryParser
{
    private static readonly JsonSerializerOptions JsonSerializerOptions = new()
    {
        PropertyNameCaseInsensitive = true
    };

    public static InvoiceReportQueryDto ParseAndNormalize(string? content)
    {
        return Normalize(ParseJson(content));
    }

    public static InvoiceReportQueryDto ParserFailure(string reason)
    {
        return new InvoiceReportQueryDto(false, null, null, null, reason);
    }

    private static InvoiceReportQueryDto ParseJson(string? content)
    {
        if (string.IsNullOrWhiteSpace(content))
            return ParserFailure("Parser returned empty content.");

        var json = ExtractJsonObject(content);
        if (json == null)
            return ParserFailure("Parser did not return a JSON object.");

        return JsonSerializer.Deserialize<InvoiceReportQueryDto>(json, JsonSerializerOptions)
               ?? ParserFailure("Parser returned invalid JSON.");
    }

    private static InvoiceReportQueryDto Normalize(InvoiceReportQueryDto query)
    {
        if (!query.IsInvoiceQuery)
            return query with { Status = null, DayOffset = null, ExactDate = null };

        var status = NormalizeStatus(query.Status);
        if (status == null)
            return ParserFailure("Parser returned an unsupported invoice status.");

        var hasExactDate = query.ExactDate.HasValue;
        int? dayOffset = hasExactDate ? null : query.DayOffset ?? 0;
        DateTime? exactDate = hasExactDate ? query.ExactDate!.Value.Date : null;

        return query with
        {
            Status = status,
            DayOffset = dayOffset,
            ExactDate = exactDate
        };
    }

    private static string? NormalizeStatus(string? status)
    {
        if (string.IsNullOrWhiteSpace(status)) return "Paid";

        return status.Trim() switch
        {
            "Pending" => "Pending",
            "Delivering" => "Delivering",
            "Delivered" => "Delivered",
            "Paid" => "Paid",
            "Cancelled" => "Cancelled",
            _ => null
        };
    }

    private static string? ExtractJsonObject(string content)
    {
        var start = content.IndexOf('{');
        var end = content.LastIndexOf('}');
        if (start < 0 || end <= start) return null;

        return content[start..(end + 1)];
    }
}
