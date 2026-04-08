using System.Security.Claims;
using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.InvoiceDetailDTOs;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Rules;
using ShoeStore.Domain.Enum;
namespace ShoeStore.Application.Services
{
    public class InvoiceService(
        IInvoiceRepository invoiceRepository,
        IUnitOfWork uow,
        ICurrentUser currentUser) : IInvoiceService
    {

        public async Task<ErrorOr<PageResult<InvoiceResponseDto>>> GetInvoiceAsync(InvoiceRequestDto request, CancellationToken token)
        {
            if (!currentUser.IsAuthenticated)
                return Error.Unauthorized("Unauthorized");

            var query = invoiceRepository.GetAll();

            if (query == null)
            {
                return Error.NotFound("Invoice not found");
            }

            // check admin or user
            if (!currentUser.IsAdmin)
                query = query.Where(i => i.PublicId == currentUser.Id);

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

        public async Task<ErrorOr<IEnumerable<InvoiceDetailResponseDto>>> GetInvoiceDetailAsync(Guid invoiceGuid, CancellationToken token)
        {
            var details = invoiceRepository.GetaInvoiceDetail(invoiceGuid);

            var result = await details.Select(d => new InvoiceDetailResponseDto
            {
                ProductName = d.ProductVariant.Product.ProductName,
                Size = d.ProductVariant.Size.Size,
                Color = d.ProductVariant.Color.ColorName,
                Quantity = d.Quantity,
                UnitPrice = d.UnitPrice
            }).ToListAsync(token);

            if (result == null || result.Count == 0)
            {
                return Error.NotFound("Invoice detail not found");
            }
            return result;
        }

        public async Task<ErrorOr<Updated>> UpdateInvoiceStateByUserAsync(Guid invoiceGuid, UpdateStateRequestDto request, CancellationToken token)
        {
            var invoice = await invoiceRepository.GetByPublicIdAsync(invoiceGuid, token);
            if (invoice == null)
            {
                return Error.NotFound("Invoice not found");
            }
            if (invoice.PublicId != currentUser.Id)
            {
                return Error.Unauthorized("Unauthorized");
            }

            if (!UpdateInvoiceStateRule.CanClientUpdateState(invoice.Status, request.Status))
                return Error.Forbidden("Client cannot change to this status");
            if (request.Status == InvoiceStatus.Paid && invoice.Payment == null)
                return Error.Validation("Cannot mark as paid without payment");

            invoice.Status = request.Status;
            invoice.UpdatedAt = DateTime.UtcNow;
            invoiceRepository.Update(invoice);
            await uow.SaveChangesAsync(token);
            return Result.Updated;
        }

        public async Task<ErrorOr<Updated>> UpdateInvoiceStateByAdminAsync(Guid invoiceGuid, UpdateStateRequestDto request, CancellationToken token)
        {
            var invoice = await invoiceRepository.GetByPublicIdAsync(invoiceGuid, token);
            if (invoice == null)
            {
                return Error.NotFound("Invoice not found");
            }
            if (!UpdateInvoiceStateRule.CanAdminUpdateState(invoice.Status, request.Status))
                return Error.Forbidden("Admin cannot change to this status");

            if (request.Status == InvoiceStatus.Paid && invoice.Payment == null)
                return Error.Validation("Cannot mark as paid without payment");

            invoice.Status = request.Status;
            invoice.UpdatedAt = DateTime.UtcNow;
            invoiceRepository.Update(invoice);
            await uow.SaveChangesAsync(token);
            return Result.Updated;
        }
    }
}
