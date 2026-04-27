using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.CartItemDTOs;
using ShoeStore.Application.Interface.CartItemInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for managing user shopping cart operations including adding, updating, and removing items.
///     Provides endpoints for frontend applications to perform cart operations with proper authorization.
///     All operations require User role authorization.
/// </summary>
/// <param name="cartItemService">Service for handling cart item operations.</param>
[Route("api/cart")]
[ApiController]
[Authorize(Roles = "User")]
public class CartController(ICartItemService cartItemService) : ControllerBase
{
    /// <summary>
    ///     Adds a new item to the user's shopping cart or increases quantity if item already exists.
    /// </summary>
    /// <remarks>
    ///     Requires User role authorization and a request body with:
    ///     - <c>userId</c>: the target user identifier
    ///     - <c>variantId</c>: the product variant identifier
    ///     - <c>quantity</c>: number of items to add (must not exceed available stock)
    ///     This operation validates that the user exists and the product variant is available.
    ///     The quantity is checked against current stock levels.
    /// </remarks>
    /// <param name="dto">Payload containing userId, variantId, and quantity.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Cart item was added successfully. Returns the updated cart item details.</response>
    /// <response code="400">Bad request; the requested quantity exceeds available stock.</response>
    /// <response code="404">Not found; the user or product variant does not exist.</response>
    /// <response code="401">Unauthorized; user must have User role authorization.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>An action result containing the cart item data on success, or an error response describing what went wrong.</returns>
    [ProducesResponseType(typeof(UserCartItemResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost]
    public async Task<IActionResult> AddUserCartItem([FromBody] AddCartItemDto dto, CancellationToken token)
    {
        var validUser = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (validUser == null || !Guid.TryParse(validUser, out var publicUserId))
            return Unauthorized(new
            {
                message = "You are not authorized to perform this action.",
                description = "Please login to your account and try again."
            });
        var result = await cartItemService.AddCartItemAsync(dto, publicUserId, token);
        var response = result.Match<IActionResult>(
            responseDto => Ok(responseDto),
            errors => errors[0].Code switch
            {
                "ProductVariant.NotFound" => NotFound(new
                {
                    code = errors[0].Code,
                    message = "Product Variant not found",
                    detail = errors[0].Description
                }),
                "CartItem.QuantityExceedsStock" => BadRequest(new
                {
                    message = "Invalid quantity",
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
    ///     Updates an existing item in the user's shopping cart with a new product variant and quantity.
    /// </summary>
    /// <remarks>
    ///     Requires User role authorization and a request body with:
    ///     - <c>cartItemId</c>: the cart item identifier to update
    ///     - <c>newProductVariantId</c>: the new product variant identifier to replace the current one
    ///     - <c>quantity</c>: the updated quantity (must not exceed available stock)
    ///     This operation validates that the cart item exists and the new product variant is available.
    ///     The new quantity is verified against the product variant's stock levels.
    /// </remarks>
    /// <param name="dto">Payload containing cartItemId, newProductVariantId, and quantity.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Cart item was updated successfully. Returns the updated cart item details.</response>
    /// <response code="400">Bad request; the requested quantity exceeds available stock for the new variant.</response>
    /// <response code="404">Not found; the cart item or product variant does not exist.</response>
    /// <response code="401">Unauthorized; user must have User role authorization.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>
    ///     An action result containing the updated cart item data on success, or an error response describing what went
    ///     wrong.
    /// </returns>
    [ProducesResponseType(typeof(UserCartItemResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPut]
    public async Task<IActionResult> UpdateUserCartItem([FromBody] UpdateCartItemDto dto,
        CancellationToken token)
    {
        var validUser = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (validUser == null || !Guid.TryParse(validUser, out _))
            return Unauthorized(new
            {
                message = "You are not authorized to perform this action.",
                description = "Please login to your account and try again."
            });
        var result = await cartItemService.UpdateCartItemAsync(dto, token);
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
    ///     Removes one or more items from the user's shopping cart.
    /// </summary>
    /// <remarks>
    ///     Requires User role authorization and a request body with:
    ///     - <c>cartItemList</c>: array of unique cart item identifiers to delete
    ///     This operation validates that all cart items exist before deletion.
    ///     If any item is not found, the entire operation fails and no items are removed.
    /// </remarks>
    /// <param name="cartItemList">A list of cart item identifiers (GUIDs) to delete.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Cart items were deleted successfully. Returns a success message.</response>
    /// <response code="404">Not found; one or more cart items do not exist in the user's cart.</response>
    /// <response code="401">Unauthorized; user must have User role authorization.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>An action result containing a success message on success, or an error response describing what went wrong.</returns>
    [ProducesResponseType(typeof(object), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost("remove-items")]
    public async Task<IActionResult> DeleteUserCartItem([FromBody] List<Guid> cartItemList, CancellationToken token)
    {
        var validUser = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (validUser == null || !Guid.TryParse(validUser, out var publicUserId))
            return Unauthorized(new
            {
                message = "You are not authorized to perform this action.",
                description = "Please login to your account and try again."
            });
        var result = await cartItemService.DeleteCartItemAsync(cartItemList, publicUserId, token);
        var response = result.Match<IActionResult>(
            _ => Ok(new { message = "Cart items deleted successfully" }),
            errors => errors[0].Code switch
            {
                "CartItem.NotFound" => NotFound(new
                {
                    message = "One or more cart items not found",
                    detail = errors[0].Description
                }),
                "User.Unauthorized" => Unauthorized(new
                {
                    message = "You are not authorized to perform this action.",
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
    ///     Retrieves all cart items belonging to the currently authenticated user.
    /// </summary>
    /// <remarks>
    ///     Requires a valid authenticated user identity from JWT claims.
    ///     This endpoint returns the user's cart items with product variant details used by the frontend cart screen.
    ///     If the user does not exist in the system, a not-found response is returned.
    /// </remarks>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Cart items were retrieved successfully. Returns the user's cart item list.</response>
    /// <response code="401">Unauthorized; user is not authenticated or identity claim is invalid.</response>
    /// <response code="404">Not found; the user does not exist.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>
    ///     An action result containing the user's cart items on success, or an error response describing what went wrong.
    /// </returns>
    [ProducesResponseType(typeof(List<UserCartItemResponseDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("user-cart-items")]
    public async Task<IActionResult> GetUserCartItem(CancellationToken token)
    {
        var validUser = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if(validUser == null || !Guid.TryParse(validUser, out var publicUserId))
            return Unauthorized(new
            {
                message = "You are not authorized to perform this action.",
                description = "Please login to your account and try again."
            });
        
        var result = await cartItemService.GetCartItemsByUserIdAsync(publicUserId, token);

        var response = result.Match<IActionResult>(
            cartItems => Ok(cartItems),
            errors => errors[0].Code switch
            {
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
            });
        return response;
    }
}