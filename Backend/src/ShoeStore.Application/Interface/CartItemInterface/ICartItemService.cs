using ErrorOr;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.CartItemDTOs;

namespace ShoeStore.Application.Interface.CartItemInterface;

public interface ICartItemService
{
    Task<ErrorOr<UserCartItemResponseDto>> UpdateCartItem(UpdateCartItemDto dto, CancellationToken token);

    Task<ErrorOr<UserCartItemResponseDto>> AddCartItem(AddCartItemDto dto,Guid userPublicId ,CancellationToken token);

    Task<ErrorOr<Success>> DeleteCartItem(List<Guid> cartItemList, CancellationToken token);
}