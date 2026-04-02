using ErrorOr;
using ShoeStore.Application.DTOs.CartItemDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class CartItemService(
    ICartItemRepository cartItemRepository,
    IUnitOfWork unitOfWork,
    IProductVariantRepository productVariantRepository,
    IUserRepository userRepository) : ICartItemService
{
    public async Task<ErrorOr<UserCartItemResponseDto>> UpdateCartItem(UpdateCartItemDto dto, CancellationToken token)
    {
        var finalQuantity = dto.Quantity;

        var cartItem = await cartItemRepository.GetCartItemByGuid(dto.CartItemId, token, true);

        if (cartItem == null) return Error.NotFound("CartItem.NotFound", "Cart item not found.");

        var productVariant = await productVariantRepository.GetByGuidAsync(dto.NewProductVariantId, token);

        if (productVariant == null)
            return Error.NotFound("ProductVariant.NotFound", "Product Variant not found.");

        if (cartItem.ProductVariantId != productVariant.Id)
        {
            var existingItem = await cartItemRepository.GetExistCartItem(cartItem.UserId, productVariant.Id, token);
            if (existingItem != null)
            {
                existingItem.Quantity += dto.Quantity;
                if (existingItem.Quantity > productVariant.Stock)
                    return Error.Validation("CartItem.QuantityExceedsStock",
                        "The quantity exceeds the available stock.");

                cartItemRepository.Update(existingItem);
                cartItemRepository.Delete(cartItem);
                await unitOfWork.SaveChangesAsync(token);
                return new UserCartItemResponseDto
                {
                    CartItemId = existingItem.PublicId,
                    Quantity = existingItem.Quantity,
                    ColorId = productVariant.ColorId,
                    ColorName = productVariant.Color?.ColorName ?? string.Empty,
                    SizeId = productVariant.SizeId,
                    Size = productVariant.Size?.Size ?? 0,
                    Price = productVariant.Price,
                    Stock = productVariant.Stock,
                    ImageUrl = productVariant.ImageUrl,
                    ProductVariantId = productVariant.PublicId,
                    ProductName = productVariant.Product?.ProductName ?? string.Empty,
                    Brand = productVariant.Product?.Brand ?? string.Empty
                };
            }

            cartItem.ProductVariantId = productVariant.Id;
        }

        cartItem.Quantity = finalQuantity;
        if (cartItem.Quantity > productVariant.Stock)
            return Error.Validation("CartItem.QuantityExceedsStock", "The quantity exceeds the available stock.");
        await unitOfWork.SaveChangesAsync(token);
        return new UserCartItemResponseDto
        {
            CartItemId = cartItem.PublicId,
            Quantity = cartItem.Quantity,
            ColorId = productVariant.ColorId,
            ColorName = productVariant.Color?.ColorName ?? string.Empty,
            SizeId = productVariant.SizeId,
            Size = productVariant.Size?.Size ?? 0,
            Price = productVariant.Price,
            Stock = productVariant.Stock,
            ImageUrl = productVariant.ImageUrl,
            ProductVariantId = productVariant.PublicId,
            ProductName = productVariant.Product?.ProductName ?? string.Empty,
            Brand = productVariant.Product?.Brand ?? string.Empty
        };
    }

    public async Task<ErrorOr<UserCartItemResponseDto>> AddCartItem(AddCartItemDto dto, CancellationToken token)
    {
        var existCartItem =
            await cartItemRepository.GetExistCartItemByGuid(dto.UserPublicId, dto.VariantPublicId, token);

        var productVariant = await productVariantRepository.GetByGuidAsync(dto.VariantPublicId, token);

        if (productVariant == null) return Error.NotFound("ProductVariant.NotFound", "Product Variant not found.");

        if (existCartItem != null)
        {
            existCartItem.Quantity += dto.Quantity; // default plus 1
            if (existCartItem.Quantity > productVariant.Stock)
                return Error.Validation("CartItem.QuantityExceedsStock", "The quantity exceeds the available stock.");
            await unitOfWork.SaveChangesAsync(token);
            return new UserCartItemResponseDto
            {
                CartItemId = existCartItem.PublicId,
                Quantity = existCartItem.Quantity,
                ColorId = productVariant.ColorId,
                ColorName = productVariant.Color?.ColorName ?? string.Empty,
                SizeId = productVariant.SizeId,
                Size = productVariant.Size?.Size ?? 0,
                Price = productVariant.Price,
                Stock = productVariant.Stock,
                ImageUrl = productVariant.ImageUrl,
                ProductVariantId = productVariant.PublicId,
                ProductName = productVariant.Product?.ProductName ?? string.Empty,
                Brand = productVariant.Product?.Brand ?? string.Empty
            };
        }

        if (dto.Quantity > productVariant.Stock)
            return Error.Validation("CartItem.QuantityExceedsStock", "The quantity exceeds the available stock.");
        var user = await userRepository.GetUserByPublicIdAsync(dto.UserPublicId, token);
        if (user == null) return Error.NotFound("User.NotFound", "User not found.");

        var newCartItem = new CartItem
        {
            UserId = user.Id,
            ProductVariantId = productVariant.Id,
            Quantity = dto.Quantity
        };
        cartItemRepository.Add(newCartItem);
        await unitOfWork.SaveChangesAsync(token);

        return new UserCartItemResponseDto
        {
            CartItemId = newCartItem.PublicId,
            Quantity = newCartItem.Quantity,
            ColorId = productVariant.ColorId,
            ColorName = productVariant.Color?.ColorName ?? string.Empty,
            SizeId = productVariant.SizeId,
            Size = productVariant.Size?.Size ?? 0,
            Price = productVariant.Price,
            Stock = productVariant.Stock,
            ImageUrl = productVariant.ImageUrl,
            ProductVariantId = productVariant.PublicId,
            ProductName = productVariant.Product?.ProductName ?? string.Empty,
            Brand = productVariant.Product?.Brand ?? string.Empty
        };
    }

    public async Task<ErrorOr<Success>> DeleteCartItem(List<Guid> cartItemList, CancellationToken token)
    {
        var result = await cartItemRepository.DeleteListOfCartItems(cartItemList, token);
        if (!result) return Error.NotFound("CartItem.NotFound", "One or more cart items were not found.");
        await unitOfWork.SaveChangesAsync(token);
        return Result.Success;
    }
}