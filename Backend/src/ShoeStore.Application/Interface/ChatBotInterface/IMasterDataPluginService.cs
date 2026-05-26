using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IMasterDataPluginService
{
    Task<MasterDataResultDto> AddNewColor(string colorName, CancellationToken token);

    Task<MasterDataResultDto> AddNewSize(decimal size, CancellationToken token);
}