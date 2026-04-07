using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;
namespace ShoeStore.Infrastructure.Repositories
{
    public class InvoiceRepository(AppDbContext context) : GenericRepository<Invoice, int>(context), IInvoiceRepository
    {
        public IQueryable<InvoiceDetail> GetaInvoiceDetailByGuid(Guid invoiceGuid)
        {
            return context.InvoiceDetails.Where(id => id.Invoice.PublicId == invoiceGuid).AsQueryable();
        }

        public IQueryable<Invoice> GetAll()
        {
            return context.Invoices.AsQueryable();
        }
    }
}
