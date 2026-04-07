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
    }
}
