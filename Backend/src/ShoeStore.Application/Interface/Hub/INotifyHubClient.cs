using ShoeStore.Application.DTOs.HubDTOs;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Interface.Hub;

public interface INotifyHubClient
{
    Task ReceivePaymentNotification(PaymentNotificationDto paymentNotification);

    Task ReceiveNotification(string invoiceCode, InvoiceStatus newStatus);
}