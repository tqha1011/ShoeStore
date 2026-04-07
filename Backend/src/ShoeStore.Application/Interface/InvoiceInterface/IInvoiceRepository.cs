using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.InvoiceInterface
{
    public interface IInvoiceRepository : IGenericRepository<Invoice, int>
    {
        IQueryable<Invoice> GetAll();
        IQueryable<InvoiceDetail> GetaInvoiceDetail(Guid invoiceGuid);
    }
}
