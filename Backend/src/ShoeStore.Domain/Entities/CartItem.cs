using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class CartItem : Entity<int>
{
    public Guid PublicId { get; set; }
    public required int UserId { get; set; }
    public User? User { get; set; }
    public required int Quantity { get; set; }
    public required int ProductVariantId { get; set; }
    public ProductVariant? ProductVariant { get; set; }
}