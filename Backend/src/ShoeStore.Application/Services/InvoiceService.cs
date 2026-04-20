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
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Services;

public class InvoiceService(
    IInvoiceRepository invoiceRepository,
    IUnitOfWork uow,
    ICurrentUser currentUser) : IInvoiceService
{
    public async Task<ErrorOr<PageResult<InvoiceResponseDto>>> GetInvoiceAsync(InvoiceRequestDto request,
        CancellationToken token)
    {
        var query = invoiceRepository.GetAll();

        // check admin or user
        if (!currentUser.IsAdmin)
            query = query.Where(i => i.User != null && i.User.PublicId == currentUser.Id);

        query = query.ApplyInvoiceFilters(request);

        var totalCount = await query.CountAsync(token);

        query = query.ApplyPagination(request.PageNumber, request.PageSize);

        var invoices = await query.Select(i => new InvoiceResponseDto
        {
            PublicId = i.PublicId,
            Username = i.User != null ? i.User.UserName : string.Empty,
            DateCreated = i.CreatedAt,
            UpdateCreated = i.UpdatedAt,
            Status = i.Status,
            PaymentName = i.Payment != null ? i.Payment.Name : string.Empty,
            Address = i.ShippingAddress,
            Phone = i.Phone,
            OrderCode = i.OrderCode,
            FinalPrice = i.FinalPrice
        }).ToListAsync(token);

        var pageResult = new PageResult<InvoiceResponseDto>
        {
            Items = invoices.Count == 0 ? [] : invoices,
            TotalCount = totalCount,
            PageNumber = request.PageNumber,
            PageSize = request.PageSize
        };
        return pageResult;
    }

    public async Task<ErrorOr<IEnumerable<InvoiceDetailResponseDto>>> GetInvoiceDetailAsync(Guid invoiceGuid,
        CancellationToken token)
    {
        var details = invoiceRepository.GetInvoiceDetail(invoiceGuid);

        var result = await details.Select(d => new InvoiceDetailResponseDto
        {
            ProductName = d.ProductVariant!.Product.ProductName,
            Size = d.ProductVariant.Size!.Size,
            Color = d.ProductVariant.Color!.ColorName,
            Quantity = d.Quantity,
            UnitPrice = d.UnitPrice,
            ImageUrl = d.ProductVariant.ImageUrl ?? string.Empty
        }).ToListAsync(token);

        if (result.Count == 0) return Error.NotFound("InvoiceDetail.NotFound", "Invoice detail not found");
        return result;
    }

    public async Task<ErrorOr<string>> UpdateInvoiceStateByUserAsync(Guid invoiceGuid, UpdateStateRequestDto request,
        CancellationToken token)
    {
        var invoice = await invoiceRepository.GetByPublicIdAsync(invoiceGuid, token);
        if (invoice == null) return Error.NotFound("Invoice.NotFound", "Invoice not found");
        if (invoice.User?.PublicId != currentUser.Id)
            return Error.Unauthorized("User.Unauthorized", "You are not authorized to update this invoice");

        if (!UpdateInvoiceStateRule.CanClientUpdateState(invoice.Status, request.Status))
            return Error.Forbidden("Invoice.Forbidden", "Client cannot change to this status");

        invoice.Status = request.Status;
        invoice.UpdatedAt = DateTime.UtcNow;
        invoiceRepository.Update(invoice);
        await uow.SaveChangesAsync(token);
        return invoice.OrderCode;
    }

    public async Task<ErrorOr<UpdateStateAdminResponseDto>> UpdateInvoiceStateByAdminAsync(Guid invoiceGuid,
        UpdateStateRequestDto request,
        CancellationToken token)
    {
        var invoice = await invoiceRepository.GetByPublicIdAsync(invoiceGuid, token);
        if (invoice == null) return Error.NotFound("Invoice.NotFound", "Invoice not found");
        if (!UpdateInvoiceStateRule.CanAdminUpdateState(invoice.Status, request.Status, invoice.PaymentId))
            return Error.Forbidden("Invoice.Forbidden", "Admin cannot change to this status");

        // check if client had paid enough for the invoice before marking as paid, only apply for online payment (paymentId = 1)
        if (request.Status == InvoiceStatus.Paid && invoice.PaymentId == (int)PaymentMethod.SePay)
        {
            var invoicePaymentResult = CheckEnoughInvoicePayment(invoice.PaymentTransactions.ToList(),
                invoice.FinalPrice);
            if (invoicePaymentResult.IsError) return invoicePaymentResult.FirstError;
        }

        invoice.Status = request.Status;
        invoice.UpdatedAt = DateTime.UtcNow;
        invoiceRepository.Update(invoice);
        await uow.SaveChangesAsync(token);
        return new UpdateStateAdminResponseDto(invoice.OrderCode, invoice.Status, invoice.User!.PublicId);
    }

    private static ErrorOr<Success> CheckEnoughInvoicePayment(List<PaymentTransaction> paymentTransactions,
        decimal finalPrice)
    {
        if (paymentTransactions.Count == 0)
            return Error.Validation("Invoice.InvalidStatus",
                "Cannot mark as paid without payment transactions for online payment");
        var totalAmount = paymentTransactions.Sum(pt => pt.Amount);
        if (totalAmount >= finalPrice)
            return Result.Success;
        return Error.Validation("Invoice.InvalidStatus",
            "Total payment amount is not enough to mark the invoice as paid");
    }
}