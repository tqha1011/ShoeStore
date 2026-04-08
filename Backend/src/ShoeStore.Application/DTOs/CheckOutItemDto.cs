namespace ShoeStore.Application.DTOs;

public sealed record CheckOutItemDto(
    Guid VariantId,
    string ProductName,
    string? ColorName,
    int Size,
    decimal UnitPrice,
    int Quantity,
    int StockAvailable,
    bool IsOutOfStock,
    decimal SubTotal);