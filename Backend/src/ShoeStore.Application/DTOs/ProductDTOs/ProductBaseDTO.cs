namespace ShoeStore.Application.DTOs.ProductDTOs;

public abstract class ProductBaseDto
{
    public string? ProductName { get; set; }
    public string? Brand { get; set; }
    public int? CategoryId { get; set; }
}
