namespace ShoeStore.Application.DTOs;

public sealed record SepayWebhookDto(
    int Id,
    string Gateway,
    string TransactionDate,
    string AccountNumber,
    string? Code,
    string Content,
    string TransferType,
    decimal TransferAmount,
    decimal Accumulated,
    string? SubAccount,
    string ReferenceCode,
    string Description);