using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.CartItemDTOs;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class CartItemRepository(AppDbContext context) : GenericRepository<CartItem, int>(context), ICartItemRepository
{
    public async Task<List<UserCartItemResponseDto>> GetCartItemsByUserIdAsync(Guid userId, CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .Where(x => x.User!.PublicId == userId)
            .Select(x => new UserCartItemResponseDto
            {
                CartItemId = x.PublicId,
                Quantity = x.Quantity,
                Brand = x.ProductVariant!.Product.Brand,
                ProductName = x.ProductVariant!.Product.ProductName,
                Price = x.ProductVariant.Price,
                ColorId = x.ProductVariant.ColorId,
                SizeId = x.ProductVariant.SizeId,
                ColorName = x.ProductVariant.Color!.ColorName,
                Size = x.ProductVariant.Size!.Size,
                ImageUrl = x.ProductVariant.ImageUrl,
                Stock = x.ProductVariant.Stock,
                ProductVariantId = x.ProductVariant.PublicId
            })
            .ToListAsync(token);
    }

    public async Task<CartItem?> GetCartItemByGuidAsync(Guid publicId, CancellationToken token,
        bool trackChanges = false)
    {
        if (trackChanges) return await DbSet.FirstOrDefaultAsync(x => x.PublicId == publicId, token);
        return await DbSet.AsNoTracking().FirstOrDefaultAsync(x => x.PublicId == publicId, token);
    }

    public async Task<CartItem?> GetExistCartItemAsync(int userId, int productVariantId, CancellationToken token)
    {
        return await DbSet.FirstOrDefaultAsync(x => x.UserId == userId && x.ProductVariantId == productVariantId,
            token);
    }

    public async Task<CartItem?> GetExistCartItemByGuidAsync(Guid publicUserId, Guid publicVariantId,
        CancellationToken token)
    {
        return await DbSet.FirstOrDefaultAsync(
            x => x.User!.PublicId == publicUserId && x.ProductVariant!.PublicId == publicVariantId, token);
    }

    public async Task<bool> DeleteListOfCartItemsAsync(List<Guid> cartItemsList, CancellationToken token)
    {
        var cartItems = await DbSet.Where(x => cartItemsList.Contains(x.PublicId))
            .Distinct()
            .ToListAsync(token);
        if (cartItems.Count != cartItemsList.Count) return false;
        DbSet.RemoveRange(cartItems);
        return true;
    }

    public void DeleteCartItem(IEnumerable<CartItem> cartItems)
    {
        DbSet.RemoveRange(cartItems);
    }
}