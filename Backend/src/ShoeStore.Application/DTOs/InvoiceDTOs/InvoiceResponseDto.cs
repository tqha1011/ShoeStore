using ShoeStore.Domain.Enum;
namespace ShoeStore.Application.DTOs.InvoiceDTOs
{
    public class InvoiceResponseDto
    {
        public Guid PublicId { get; set; }
        public string Username { get; set; } = string.Empty;
        public DateTime DateCreated { get; set; }
        public DateTime? UpdateCreated { get; set; } // Accept null value if the invoice has not been updated yet
        public InvoiceStatus Status { get; set; }
        public string PaymentName { get; set; } = string.Empty;
        public string Address { get; set; } = string.Empty;
        public string Phone { get; set; } = string.Empty;
    }
}
