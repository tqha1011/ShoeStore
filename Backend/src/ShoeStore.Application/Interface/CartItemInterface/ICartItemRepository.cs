using ShoeStore.Application.DTOs.CartItemDTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.CartItemInterface;

public interface ICartItemRepository : IGenericRepository<CartItem, int>
{
    Task<List<UserCartItemResponseDto>> GetCartItemsByUserIdAsync(Guid userId, CancellationToken token);

    Task<CartItem?> GetCartItemByGuid(Guid publicId, CancellationToken token, bool trackChanges = false);

    Task<CartItem?> GetExistCartItem(int userId, int productVariantId, CancellationToken token);

    Task<CartItem?> GetExistCartItemByGuid(Guid publicUserId, Guid publicVariantId, CancellationToken token);

    Task<bool> DeleteListOfCartItems(List<Guid> cartItemsList, CancellationToken token);
}