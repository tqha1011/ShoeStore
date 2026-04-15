namespace ShoeStore.Application.DTOs.StatisticsDto;

public sealed record SummaryDto(int InvoiceId, decimal InvoiceTotal, DateTime CreatedAt);