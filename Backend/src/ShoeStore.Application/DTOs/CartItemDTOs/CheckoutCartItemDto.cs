namespace ShoeStore.Application.DTOs.CartItemDTOs;

public sealed record CheckoutCartItemDto(Guid CartItemId, int Quantity);