using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class ProductSize : Entity<int>
{
    public required int Size { get; set; }
}