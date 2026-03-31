namespace ShoeStore.Application.DTOs;

public sealed record AddCartItemDto(Guid UserPublicId, Guid VariantPublicId, int Quantity = 1);