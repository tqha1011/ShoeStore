using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class Product : Entity<int>
{
    public Guid PublicId { get; set; } = Guid.NewGuid();
    public required string ProductName { get; set; }
    public string? Brand { get; set; }
    
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    public ICollection<ProductVariant> ProductVariants { get; set; } = new List<ProductVariant>();
}