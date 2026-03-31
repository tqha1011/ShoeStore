namespace ShoeStore.Application.DTOs;

public sealed record CheckoutCartItemDto(Guid CartItemId, int Quantity);