using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.DTOs.InvoiceDTOs
{
    public class InvoiceRequestDto
    {
        private int pageNumber = 1;
        public int PageNumber
        {
            get => pageNumber;
            set => pageNumber = value < 1 ? 1 : value;
        }

        private int pageSize = 10;
        public int PageSize
        {
            get => pageSize;
            set => pageSize = value < 1 ? 10 : value;
        }
        public InvoiceStatus? Status { get; set; }
    }
}
