namespace ShoeStore.Application.DTOs.CartItemDTOs;

public sealed record AddCartItemDto(Guid UserPublicId, Guid VariantPublicId, int Quantity = 1);