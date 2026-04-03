using ShoeStore.Domain.Enum;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Domain.Entities;
namespace ShoeStore.Application.Extensions
{
    public static class InvoiceQueryExtensions
    {
        public static IQueryable<Invoice> ApplyInvoiceFilters(this IQueryable<Invoice> query, InvoiceRequestDto request)
        {
            if (request.Status.HasValue)
            {
                query = query.Where(i => i.Status == request.Status.Value);
            }
            return query;
        }
    }
}