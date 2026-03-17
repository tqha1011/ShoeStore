using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class CartItem(int id) : Entity<int>(id)
{
    public required int UserId { get; set; }
    public required User User { get; set; }
    public required int Quantity { get; set; }
    public required int ProductVariantId { get; set; }
    public required ProductVariant ProductVariant { get; set; }
}