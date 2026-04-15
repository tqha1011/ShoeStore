using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.InvoiceDTOs
{
    public class UpdateStateRequestDto
    {
        public InvoiceStatus Status { get; set; }
    }
}
