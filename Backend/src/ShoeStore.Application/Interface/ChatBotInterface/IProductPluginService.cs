using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IProductPluginService
{
    Task<SearchResultDto> SearchProduct(string keyword, CancellationToken token);

    Task<AddVariantResultDto> AddNewVariant(Guid productPubicId, decimal size, string colorName, int stock,
        decimal price, string? imageUrl = null, CancellationToken token = default);
}