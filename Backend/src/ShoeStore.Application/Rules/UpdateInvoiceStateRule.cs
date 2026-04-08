using ShoeStore.Domain.Enum;
using ShoeStore.Domain.Entities;
namespace ShoeStore.Application.Rules
{
    public static class UpdateInvoiceStateRule
    {
        public static bool CanClientUpdateState(InvoiceStatus currentStatus, InvoiceStatus newStatus)
        {
            // Define valid state transitions
            return (currentStatus, newStatus) switch
            {
                (InvoiceStatus.Pending, InvoiceStatus.Paid) => true,
                (InvoiceStatus.Pending, InvoiceStatus.Canceled) => true,
                (InvoiceStatus.Paid, InvoiceStatus.Canceled) => true,
                _ => false
            };
        }
        public static bool CanAdminUpdateState(InvoiceStatus currentStatus, InvoiceStatus newStatus)
        {
            // Admin can change to any status except from Canceled to Paid
            return (currentStatus, newStatus) switch
            {
                (InvoiceStatus.Pending, InvoiceStatus.Canceled) => true,
                (InvoiceStatus.Pending, InvoiceStatus.Paid) => true,
                (InvoiceStatus.Paid, InvoiceStatus.Canceled) => true,
                (InvoiceStatus.Paid, InvoiceStatus.Delivering) => true,
                _ => true
            };
        }
    }
}
