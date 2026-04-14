namespace ShoeStore.Application.DTOs.CartItemDTOs;

public sealed record AddCartItemDto(Guid VariantPublicId, int Quantity = 1);