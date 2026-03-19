using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class Product : Entity<int>
{
    public required string ProductName { get; set; }
    public string? Brand { get; set; }
    
    public ICollection<ProductVariant> ProductVariants { get; set; } = new List<ProductVariant>();
}