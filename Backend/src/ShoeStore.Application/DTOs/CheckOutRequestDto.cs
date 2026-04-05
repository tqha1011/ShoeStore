namespace ShoeStore.Application.DTOs;

public sealed record CheckOutRequestDto(Guid VariantId, int Quantity = 1);