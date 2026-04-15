namespace ShoeStore.Application.DTOs.ProductDTOs;

public class ProductBaseDto
{
    public required string ProductName { get; set; }
    public string? Brand { get; set; }

    public int CategoryId { get; set; }
}