namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record InvoiceReportQueryDto(
    bool IsInvoiceQuery,
    string? Status,
    int? DayOffset,
    DateTime? ExactDate,
    string? Reason);
