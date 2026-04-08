namespace ShoeStore.Application.DTOs.CheckOutDTOs;

public sealed record CheckOutRequestDto(Guid VariantId, int Quantity = 1);