using System.Security.Claims;
using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.DTOs.InvoiceDetailDTOs;

namespace ShoeStore.Application.Interface.InvoiceInterface
{
    public interface IInvoiceService
    {
        Task<ErrorOr<PageResult<InvoiceResponseDto>>> GetInvoiceAsync(InvoiceRequestDto request, ClaimsPrincipal user, CancellationToken token);
        Task<ErrorOr<IEnumerable<InvoiceDetailResponseDto>>> GetInvoiceDetailAsync(Guid invoiceGuid, CancellationToken token);
    }
}

