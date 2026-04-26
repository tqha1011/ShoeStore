namespace ShoeStore.Application.DTOs.ProductDTOs;

public class CreateProductDto
{
    public string ProductName { get; set; } = string.Empty;
    public int CategoryId { get; set; }
}