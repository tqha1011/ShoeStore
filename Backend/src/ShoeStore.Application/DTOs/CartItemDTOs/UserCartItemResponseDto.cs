namespace ShoeStore.Application.DTOs.CartItemDTOs;

public sealed record UserCartItemResponseDto
{
    public Guid CartItemId { get; set; }

    public Guid? ProductVariantId { get; set; }

    public string? ProductName { get; set; }

    public string? Brand { get; set; }

    public string? ImageUrl { get; set; }

    public decimal Price { get; set; }

    public int Stock { get; set; }

    public int SizeId { get; set; }

    public decimal Size { get; set; }

    public int ColorId { get; set; }

    public string? ColorName { get; set; }
    public int Quantity { get; set; }
}