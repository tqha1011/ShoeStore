using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.InvoiceDetailDTOs;
using ShoeStore.Application.DTOs.InvoiceDTOs;

namespace ShoeStore.Application.Interface.InvoiceInterface;

public interface IInvoiceService
{
    Task<ErrorOr<PageResult<InvoiceResponseDto>>> GetInvoiceAsync(InvoiceRequestDto request, CancellationToken token);

    Task<ErrorOr<IEnumerable<InvoiceDetailResponseDto>>> GetInvoiceDetailAsync(Guid invoiceGuid,
        CancellationToken token);

    Task<ErrorOr<string>> UpdateInvoiceStateByUserAsync(Guid invoiceGuid, UpdateStateRequestDto request,
        CancellationToken token);

    Task<ErrorOr<UpdateStateAdminResponseDto>> UpdateInvoiceStateByAdminAsync(Guid invoiceGuid,
        UpdateStateRequestDto request,
        CancellationToken token);
}