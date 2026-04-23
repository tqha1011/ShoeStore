using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Rules;

public static class UpdateInvoiceStateRule
{
    public static bool CanClientUpdateState(InvoiceStatus currentStatus, InvoiceStatus newStatus)
    {
        // Define valid state transitions
        return (currentStatus, newStatus) switch
        {
            (InvoiceStatus.Pending, InvoiceStatus.Cancelled) => true,
            _ => false
        };
    }

    public static bool CanAdminUpdateState(InvoiceStatus currentStatus, InvoiceStatus newStatus, int paymentId)
    {
        var method = (PaymentMethod)paymentId;
        return (method, currentStatus, newStatus) switch
        {
            // both 2 payment method
            (_, InvoiceStatus.Pending, InvoiceStatus.Cancelled) => true,
            (_, InvoiceStatus.Paid, InvoiceStatus.Cancelled) => true,

            // COD payment method (paymentId = 2)
            (PaymentMethod.Cod, InvoiceStatus.Delivering, InvoiceStatus.Paid) => true,
            (PaymentMethod.Cod, InvoiceStatus.Pending, InvoiceStatus.Delivering) => true,
            (PaymentMethod.Cod, InvoiceStatus.Paid, InvoiceStatus.Delivered) => true,

            // SePay payment method (paymentId = 1)
            (PaymentMethod.SePay, InvoiceStatus.Paid, InvoiceStatus.Delivering) => true,
            (PaymentMethod.SePay, InvoiceStatus.Pending, InvoiceStatus.Paid) => true,
            (PaymentMethod.SePay, InvoiceStatus.Delivering, InvoiceStatus.Delivered) => true,
            _ => false
        };
    }
}