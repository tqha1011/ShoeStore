using ShoeStore.Application.DTOs.HubDTOs;

namespace ShoeStore.Application.Interface.Hub;

public interface INotifyHubClient
{
    public Task ReceiveNotification(PaymentNotificationDto paymentNotification);
}