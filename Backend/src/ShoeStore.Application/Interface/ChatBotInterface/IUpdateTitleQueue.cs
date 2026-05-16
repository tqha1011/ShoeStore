using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IUpdateTitleQueue
{
    ValueTask EnqueueAsync(UpdateTitleRequestDto requestDto, CancellationToken cancellationToken);

    ValueTask<UpdateTitleRequestDto> DequeueAsync(CancellationToken cancellationToken);
}