using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class Payment : Entity<int>
{
    public required string Name { get; set; }

    public string? Code { get; set; }
}