using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.CartItemDTOs;
using ShoeStore.Application.Interface.CartItemInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for add, update , delete user's cart items
/// </summary>
[Route("api/cart")]
[ApiController]
public class CartController(ICartItemService cartItemService) : ControllerBase
{
    /// <summary>
    ///     Add cart item to user's cart
    ///     This required public userId , public variantId, quantity
    /// </summary>
    /// <remarks>
    ///     Requires a request body with:
    ///     - <c>userId</c>: the target user identifier
    ///     - <c>variantId</c>: the product variant identifier
    ///     - <c>quantity</c>: number of items to add
    /// </remarks>
    /// <param name="dto">Payload containing userId, variantId, and quantity.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Cart item was added successfully.</response>
    /// <response code="400">The requested quantity exceeds available stock.</response>
    /// <response code="404">The user or product variant was not found.</response>
    /// <response code="500">An unexpected server error occurred.</response>
    /// <returns>An action result containing success data or an error response.</returns>
    [HttpPost]
    public async Task<IActionResult> AddUserCartItem([FromBody] AddCartItemDto dto, CancellationToken token)
    {
        var result = await cartItemService.AddCartItem(dto, token);
        var response = result.Match<IActionResult>(
            responseDto => Ok(responseDto),
            errors => errors[0].Code switch
            {
                "ProductVariant.NotFound" => NotFound(new
                {
                    message = "Product Variant not found",
                    detail = errors[0].Description
                }),
                "CartItem.QuantityExceedsStock" => BadRequest(new
                {
                    message = "The quantity exceeds the available stock",
                    detail = errors[0].Description
                }),
                "User.NotFound" => NotFound(new
                {
                    message = "User not found",
                    detail = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "Something went wrong",
                    detail = errors[0].Description
                })
            }
        );
        return response;
    }

    /// <summary>
    ///     Updates an existing item in the user's cart.
    /// </summary>
    /// <remarks>
    ///     Requires a request body with:
    ///     - <c>cartItemId</c>: the cart item identifier to update
    ///     - <c>newProductVariantId</c>: the new product variant identifier
    ///     - <c>quantity</c>: the updated quantity
    /// </remarks>
    /// <param name="dto">Payload containing cartItemId, newProductVariantId, and quantity.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Cart item was updated successfully.</response>
    /// <response code="400">The requested quantity exceeds available stock.</response>
    /// <response code="404">The cart item or product variant was not found.</response>
    /// <response code="500">An unexpected server error occurred.</response>
    /// <returns>An action result containing success data or an error response.</returns>
    [HttpPut]
    public async Task<IActionResult> UpdateUserCartItem([FromBody] UpdateCartItemDto dto,
        CancellationToken token)
    {
        var result = await cartItemService.UpdateCartItem(dto, token);
        var response = result.Match<IActionResult>(
            responseDto => Ok(responseDto),
            errors => errors[0].Code switch
            {
                "ProductVariant.NotFound" => NotFound(new
                {
                    message = "Product Variant not found",
                    detail = errors[0].Description
                }),
                "CartItem.QuantityExceedsStock" => BadRequest(new
                {
                    message = "The quantity exceeds the available stock",
                    detail = errors[0].Description
                }),
                "CartItem.NotFound" => NotFound(new
                {
                    message = "Cart item not found",
                    detail = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "Something went wrong",
                    detail = errors[0].Description
                })
            }
        );
        return response;
    }

    /// <summary>
    ///     Deletes one or more items from the user's cart.
    /// </summary>
    /// <remarks>
    ///     Requires a request body with:
    ///     - <c>cartItemList</c>: array of cart item identifiers to delete
    /// </remarks>
    /// <param name="cartItemList">A list of cart item identifiers to delete.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Cart items were deleted successfully.</response>
    /// <response code="404">One or more cart items were not found.</response>
    /// <response code="500">An unexpected server error occurred.</response>
    /// <returns>An action result containing a success message or an error response.</returns>
    [HttpPost("remove-items")]
    public async Task<IActionResult> DeleteUserCartItem([FromBody] List<Guid> cartItemList, CancellationToken token)
    {
        var result = await cartItemService.DeleteCartItem(cartItemList, token);
        var response = result.Match<IActionResult>(
            _ => Ok(new { message = "Cart items deleted successfully" }),
            errors => errors[0].Code switch
            {
                "CartItem.NotFound" => NotFound(new
                {
                    message = "One or more cart items not found",
                    detail = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "Something went wrong",
                    detail = errors[0].Description
                })
            }
        );
        return response;
    }
}