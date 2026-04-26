using ErrorOr;
using ShoeStore.Application.DTOs.CartItemDTOs;

namespace ShoeStore.Application.Interface.CartItemInterface;

public interface ICartItemService
{
    Task<ErrorOr<UserCartItemResponseDto>> UpdateCartItemAsync(UpdateCartItemDto dto, CancellationToken token);

    Task<ErrorOr<UserCartItemResponseDto>> AddCartItemAsync(AddCartItemDto dto, Guid userPublicId,
        CancellationToken token);

    Task<ErrorOr<Success>> DeleteCartItemAsync(List<Guid> cartItemList, Guid publicUserId, CancellationToken token);
}