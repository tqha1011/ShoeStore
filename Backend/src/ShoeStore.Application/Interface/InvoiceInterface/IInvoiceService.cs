using System.Security.Claims;
using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.InvoiceDTOs;

namespace ShoeStore.Application.Interface.InvoiceInterface
{
    public interface IInvoiceService
    {
        Task<ErrorOr<PageResult<InvoiceResponseDto>>> GetInvoiceAsync(InvoiceRequestDto request, ClaimsPrincipal user, CancellationToken token);
    }
}
