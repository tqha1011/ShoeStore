using System.Threading.Channels;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface.ChatBotInterface;

namespace ShoeStore.Application.Services;

public class UpdateTitleService : IUpdateTitleQueue
{
    private readonly Channel<UpdateTitleRequestDto> _channel;

    public UpdateTitleService()
    {
        var option = new BoundedChannelOptions(15)
        {
            FullMode = BoundedChannelFullMode.Wait
        };
        _channel = Channel.CreateBounded<UpdateTitleRequestDto>(option);
    }

    public async ValueTask EnqueueAsync(UpdateTitleRequestDto requestDto, CancellationToken cancellationToken)
    {
        await _channel.Writer.WriteAsync(requestDto, cancellationToken);
    }

    public async ValueTask<UpdateTitleRequestDto> DequeueAsync(CancellationToken cancellationToken)
    {
        return await _channel.Reader.ReadAsync(cancellationToken);
    }
}