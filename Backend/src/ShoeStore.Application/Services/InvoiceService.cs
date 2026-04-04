using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.Common;
using ErrorOr;
using ShoeStore.Application.DTOs;
using System.Security.Claims;
using Microsoft.EntityFrameworkCore;

namespace ShoeStore.Application.Services
{
    public class InvoiceService(IInvoiceRepository invoiceRepository,
        IUnitOfWork uow,
        ICurrentUser currentUser) : IInvoiceService
    {

        public async Task<ErrorOr<PageResult<InvoiceResponseDto>>> GetInvoiceAsync(InvoiceRequestDto request, ClaimsPrincipal user, CancellationToken token)
        {
            if (!currentUser.IsAuthenticated)
                return Error.Unauthorized("Unauthorized");

            var query = invoiceRepository.GetAll();

            if(query == null)
            {
                return Error.NotFound("Invoice not found");
            }

            // check admin or user
            if (!currentUser.IsAdmin)
                query = query.Where(i => i.UserId == currentUser.Id);

            query = query.ApplyInvoiceFilters(request);

            var totalCount = await query.CountAsync(token);

            query = query.ApplyPagination(request.PageNumber, request.PageSize);

            var invoices = await query.Select(i => new InvoiceResponseDto
            {
                Id = i.Id,
                Username = i.User.UserName,
                DateCreated = i.CreatedAt,
                UpdateCreated = i.UpdatedAt,
                Status = i.Status,
                PaymentName = i.Payment.Name,
                Address = i.ShippingAddress,
                Phone = i.Phone
            }).ToListAsync(token);

            var pageResult = new PageResult<InvoiceResponseDto>
            {
                Items = invoices,
                TotalCount = totalCount,
                PageNumber = request.PageNumber,
                PageSize = request.PageSize
            };
            return pageResult;
        }
    }
}
