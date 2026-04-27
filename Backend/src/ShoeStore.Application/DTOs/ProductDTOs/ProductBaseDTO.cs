namespace ShoeStore.Application.DTOs.ProductDTOs;

public abstract class ProductBaseDto
{
    public string ProductName { get; set; } = string.Empty;
    public string Brand { get; set; } = string.Empty;
    public int? CategoryId { get; set; }
}
