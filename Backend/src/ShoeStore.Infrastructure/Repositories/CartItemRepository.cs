using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs;
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

    public async Task<CartItem?> GetCartItemByGuid(Guid publicId, CancellationToken token)
    {
        return await DbSet.AsNoTracking().FirstOrDefaultAsync(x => x.PublicId == publicId, token);
    }
}