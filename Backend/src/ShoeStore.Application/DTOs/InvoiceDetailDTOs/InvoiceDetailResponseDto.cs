
namespace ShoeStore.Application.DTOs.InvoiceDetailDTOs
{
    public class InvoiceDetailResponseDto
    {
        public string ProductName { get; set; } =  string.Empty;
        public decimal Size { get; set; } = 0;
        public string Color { get; set; } = string.Empty;
        public int Quantity { get; set; } = 0;
        public decimal UnitPrice { get; set; } = 0;
    }
}
