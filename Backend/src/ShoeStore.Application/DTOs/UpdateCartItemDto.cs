namespace ShoeStore.Application.DTOs;

public sealed record UpdateCartItemDto
{
    public Guid CartItemId { get; set; }
    public required Guid NewProductVariantId { get; init; }
    public required int Quantity { get; set; }
}