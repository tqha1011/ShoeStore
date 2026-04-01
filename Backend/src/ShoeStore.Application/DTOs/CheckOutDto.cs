namespace ShoeStore.Application.DTOs;

public sealed record CheckOutDto(Guid VariantId, int Quantity);