using System.Threading.Channels;
using ShoeStore.Application.DTOs.VoucherDTOs;
using ShoeStore.Application.Interface.Notification;

namespace ShoeStore.Infrastructure.Notification;

public class NotificationQueue : INotificationQueue
{
    private readonly Channel<VoucherNotificationDto> _queue;

    public NotificationQueue()
    {
        var options = new BoundedChannelOptions(1000)
        {
            FullMode = BoundedChannelFullMode.Wait
        };
        _queue = Channel.CreateBounded<VoucherNotificationDto>(options);
    }

    public async ValueTask EnqueueAsync(VoucherNotificationDto voucherNotificationDto, CancellationToken token)
    {
        await _queue.Writer.WriteAsync(voucherNotificationDto, token);
    }

    public async ValueTask<VoucherNotificationDto> DequeueAsync(CancellationToken token)
    {
        return await _queue.Reader.ReadAsync(token);
    }
}