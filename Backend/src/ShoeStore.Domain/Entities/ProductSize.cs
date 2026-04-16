using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class ProductSize : Entity<int>
{
    public required decimal Size { get; set; }
}