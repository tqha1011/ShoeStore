using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.CartItemDTOs;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class CartItemService(
    ICartItemRepository cartItemRepository,
    IUnitOfWork unitOfWork,
    IProductVariantRepository productVariantRepository,
    IUserRepository userRepository) : ICartItemService
{
    public async Task<ErrorOr<UserCartItemResponseDto>> UpdateCartItemAsync(UpdateCartItemDto dto,
        CancellationToken token)
    {
        var finalQuantity = dto.Quantity;

        var cartItem = await cartItemRepository.GetCartItemByGuidAsync(dto.CartItemId, token, true);

        if (cartItem == null) return Error.NotFound("CartItem.NotFound", "Cart item not found.");

        var productVariant = await productVariantRepository.GetByGuidAsync(dto.NewProductVariantId, token);

        if (productVariant == null)
            return Error.NotFound("ProductVariant.NotFound", "Product Variant not found.");

        if (cartItem.ProductVariantId != productVariant.Id)
        {
            var existingItem =
                await cartItemRepository.GetExistCartItemAsync(cartItem.UserId, productVariant.Id, token);
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
                    Brand = productVariant.Product?.Brand ?? string.Empty,
                    IsSelling = productVariant.IsSelling
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
            Brand = productVariant.Product?.Brand ?? string.Empty,
            IsSelling = productVariant.IsSelling
        };
    }

    public async Task<ErrorOr<Success>> DeleteCartItemAsync(List<Guid> cartItemList, Guid publicUserId,
        CancellationToken token)
    {
        var result = await cartItemRepository.GetListOfCartItemsAsync(cartItemList, token);
        if (result.Count != cartItemList.Count)
            return Error.NotFound("CartItem.NotFound", "One or more cart items not found.");

        var validCartItems = result.Where(cartItem => cartItem.User?.PublicId == publicUserId).ToList();
        if (validCartItems.Count != cartItemList.Count)
            return Error.Unauthorized("User.Unauthorized",
                "You are not authorized to delete one or more of these cart items.");
        cartItemRepository.DeleteListCartItem(validCartItems);
        await unitOfWork.SaveChangesAsync(token);
        return Result.Success;
    }

    public async Task<ErrorOr<UserCartItemResponseDto>> AddCartItemAsync(AddCartItemDto dto, Guid userPublicId,
        CancellationToken token)
    {
        var user = await userRepository.GetUserByPublicIdAsync(userPublicId, token);
        if (user == null) return Error.NotFound("User.NotFound", "User not found.");

        var existCartItem =
            await cartItemRepository.GetExistCartItemByGuidAsync(userPublicId, dto.VariantPublicId, token);

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
                Brand = productVariant.Product?.Brand ?? string.Empty,
                IsSelling = productVariant.IsSelling
            };
        }

        if (dto.Quantity > productVariant.Stock)
            return Error.Validation("CartItem.QuantityExceedsStock", "The quantity exceeds the available stock.");

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
            Brand = productVariant.Product?.Brand ?? string.Empty,
            IsSelling = productVariant.IsSelling
        };
    }

    public async Task<ErrorOr<List<UserCartItemResponseDto>>> GetCartItemsByUserIdAsync(Guid userPublicId,
        CancellationToken token)
    {
        var user = await userRepository.CheckUserExistsAsync(userPublicId, token);
        if (!user) return Error.NotFound("User.NotFound", "User not found.");
        
        var cartItems = cartItemRepository.GetCartItemsByUserId(userPublicId);
        var response = await cartItems.Select(x => new UserCartItemResponseDto
        {
            CartItemId = x.PublicId,
            Quantity = x.Quantity,
            Brand = x.ProductVariant!.Product.Brand ?? string.Empty,
            ProductName = x.ProductVariant.Product.ProductName,
            Price = x.ProductVariant!.Price,
            ColorId = x.ProductVariant.ColorId,
            SizeId = x.ProductVariant.SizeId,
            ColorName = x.ProductVariant!.Color.ColorName,
            Size = x.ProductVariant.Size!.Size,
            ImageUrl = x.ProductVariant.ImageUrl ?? string.Empty,
            Stock = x.ProductVariant.Stock,
            ProductVariantId = x.ProductVariant.PublicId,
            IsSelling = x.ProductVariant.IsSelling
        }).ToListAsync(token);
        return response;
    }
}