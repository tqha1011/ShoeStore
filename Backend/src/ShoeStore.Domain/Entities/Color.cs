using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class Color(int id) : Entity<int>(id)
{
    public required string ColorName { get; set; }
    public string? ColorCode { get; set; }
    
    /// <summary>
    /// Gets the collection of products in each color
    /// </summary>
    public ICollection<ProductVariant> ProductVariants { get; set; } = new List<ProductVariant>();
}