using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class Payment(int id) : Entity<int>(id)
{
    public required string Name { get; set; }
}