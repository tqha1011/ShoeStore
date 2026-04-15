namespace ShoeStore.Application.DTOs.CheckOutDTOs;

public sealed record CheckOutItemDto(
    Guid VariantId,
    string ProductName,
    string? ColorName,
    decimal Size,
    decimal UnitPrice,
    int Quantity,
    int StockAvailable,
    bool IsOutOfStock,
    decimal SubTotal);