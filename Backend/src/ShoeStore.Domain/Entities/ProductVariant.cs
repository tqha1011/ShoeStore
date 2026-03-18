using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class ProductVariant(int id) : Entity<int>(id)
{
    public required int SizeId { get; set; }
    public required ProductSize Size { get; set; } // foreign key relationship to ProductSize entity
    public required int ProductId { get; set; }
    public required Product Product { get; set; } // foreign key relationship to Product entity
    public required int ColorId { get; set; }
    public required Color Color { get; set; } // foreign key relationship to Color entity
    public required int Stock { get; set; }
    public required bool IsSelling { get; set; }
    public string? ImageUrl { get; set; }
    public required decimal Price { get; set; }
    public required bool IsDeleted { get; set; } = false;
}