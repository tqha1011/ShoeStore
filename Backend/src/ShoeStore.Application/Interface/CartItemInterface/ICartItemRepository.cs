using ShoeStore.Application.DTOs.CartItemDTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.CartItemInterface;

public interface ICartItemRepository : IGenericRepository<CartItem, int>
{
    Task<List<UserCartItemResponseDto>> GetCartItemsByUserIdAsync(Guid userId, CancellationToken token);

    Task<CartItem?> GetCartItemByGuidAsync(Guid publicId, CancellationToken token, bool trackChanges = false);

    Task<CartItem?> GetExistCartItemAsync(int userId, int productVariantId, CancellationToken token);

    Task<CartItem?> GetExistCartItemByGuidAsync(Guid publicUserId, Guid publicVariantId, CancellationToken token);

    Task<bool> DeleteListOfCartItemsAsync(List<Guid> cartItemsList, CancellationToken token);
}