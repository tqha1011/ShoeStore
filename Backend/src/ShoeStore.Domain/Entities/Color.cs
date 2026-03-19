using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class Color : Entity<int>
{
    public required string ColorName { get; set; }
    public string? HexCode { get; set; }
}