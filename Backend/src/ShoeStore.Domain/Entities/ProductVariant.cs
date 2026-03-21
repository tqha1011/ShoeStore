using ShoeStore.Domain.Common;

namespace ShoeStore.Domain.Entities;

public class ProductVariant : Entity<int>
{
    public Guid PublicId { get; set; }
    public int SizeId { get; set; }
    public ProductSize? Size { get; set; } // foreign key relationship to ProductSize entity
    public required int ProductId { get; set; }
    public required Product Product { get; set; } // foreign key relationship to Product entity
    public int? ColorId { get; set; }
    public Color? Color { get; set; } // foreign key relationship to Color entity
    public required int Stock { get; set; }
    public required bool IsSelling { get; set; }
    public string? ImageUrl { get; set; }
    public required decimal Price { get; set; }
    public required bool IsDeleted { get; set; } = false;
}