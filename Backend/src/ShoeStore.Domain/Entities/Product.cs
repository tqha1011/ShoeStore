using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class Product(int id) : Entity<int>(id)
{
    public required string ProductName { get; set; }
    public string? Brand { get; set; }
    
    public ICollection<ProductVariant> ProductVariants { get; set; } = new List<ProductVariant>();
}