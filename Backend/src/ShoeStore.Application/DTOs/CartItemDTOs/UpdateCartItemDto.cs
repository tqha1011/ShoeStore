namespace ShoeStore.Application.DTOs.CartItemDTOs;

public sealed record UpdateCartItemDto
{
    public Guid CartItemId { get; set; }
    public required int Quantity { get; set; }
}