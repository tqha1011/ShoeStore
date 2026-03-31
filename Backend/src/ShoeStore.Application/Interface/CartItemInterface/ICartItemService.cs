using ErrorOr;
using ShoeStore.Application.DTOs;

namespace ShoeStore.Application.Interface.CartItemInterface;

public interface ICartItemService
{
    public Task<ErrorOr<UserCartItemResponseDto>> UpdateCartItem(UpdateCartItemDto dto, CancellationToken token);

    public Task<ErrorOr<UserCartItemResponseDto>> AddCartItem(AddCartItemDto dto, CancellationToken token);
}