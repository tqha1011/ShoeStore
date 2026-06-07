namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record InvoiceResultDto(
    string Status,
    string Message,
    List<InvoiceDataDto> Result,
    int TotalInvoice,
    decimal TotalRevenue);

public sealed record InvoiceDataDto(
    string InvoiceCode,
    string Status,
    decimal Price);