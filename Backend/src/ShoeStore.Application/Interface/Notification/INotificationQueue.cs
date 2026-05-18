using ShoeStore.Application.DTOs.VoucherDTOs;

namespace ShoeStore.Application.Interface.Notification;

public interface INotificationQueue
{
    ValueTask EnqueueAsync(VoucherNotificationDto voucherNotificationDto, CancellationToken token);

    ValueTask<VoucherNotificationDto> DequeueAsync(CancellationToken token);
}