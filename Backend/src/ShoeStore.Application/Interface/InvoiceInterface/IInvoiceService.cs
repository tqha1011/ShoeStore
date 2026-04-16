using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.DTOs.InvoiceDetailDTOs;

namespace ShoeStore.Application.Interface.InvoiceInterface
{
    public interface IInvoiceService
    {
        Task<ErrorOr<PageResult<InvoiceResponseDto>>> GetInvoiceAsync(InvoiceRequestDto request, CancellationToken token);
        Task<ErrorOr<IEnumerable<InvoiceDetailResponseDto>>> GetInvoiceDetailAsync(Guid invoiceGuid, CancellationToken token);
        Task<ErrorOr<string>> UpdateInvoiceStateByUserAsync(Guid invoiceGuid, UpdateStateRequestDto request,
            CancellationToken token);
        Task<ErrorOr<Updated>> UpdateInvoiceStateByAdminAsync(Guid invoiceGuid, UpdateStateRequestDto request, CancellationToken token);

    }
}

