using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class CartItemRepository(AppDbContext context) : GenericRepository<CartItem, int>(context), ICartItemRepository
{
    public IQueryable<CartItem> GetCartItemsByUserId(Guid userId)
    {
        return DbSet.AsNoTracking().Where(x => x.User!.PublicId == userId).IgnoreQueryFilters();
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

    public async Task<List<CartItem>> GetListOfCartItemsAsync(List<Guid> cartItemsList, CancellationToken token)
    {
        var cartItems = await DbSet.Include(x => x.User)
            .Where(x => cartItemsList.Contains(x.PublicId))
            .Distinct()
            .ToListAsync(token);
        return cartItems;
    }

    public void DeleteListCartItem(IEnumerable<CartItem> cartItems)
    {
        DbSet.RemoveRange(cartItems);
    }
}