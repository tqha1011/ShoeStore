using ShoeStore.Application.DTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.CartItemInterface;

public interface ICartItemRepository : IGenericRepository<CartItem, int>
{
    Task<List<UserCartItemResponseDto>> GetCartItemsByUserIdAsync(Guid userId, CancellationToken token);
    
    Task<CartItem?> GetCartItemByGuid(Guid publicId, CancellationToken token);
}