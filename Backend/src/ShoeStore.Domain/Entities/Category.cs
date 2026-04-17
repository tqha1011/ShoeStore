using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class Category : Entity<int>
{
    public required string Name { get; set; }
    public string? Description { get; set; }

    public ICollection<Product> Products { get; set; } = new List<Product>();
}