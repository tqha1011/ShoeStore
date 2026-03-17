using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class ProductSize(int id) : Entity<int>(id)
{
    public required int Size { get; set; }
    
    public ICollection<ProductVariant> ProductVariants { get; set; } = new List<ProductVariant>();
}