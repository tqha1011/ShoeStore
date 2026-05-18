using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class UserRepository(AppDbContext context) : GenericRepository<User, int>(context), IUserRepository
{
    public async Task<bool> IsEmailExistAsync(string email, CancellationToken token)
    {
        return await DbSet.AnyAsync(x => x.Email == email, token);
    }

    public async Task<User?> GetUserByEmailAsync(string email, CancellationToken token)
    {
        return await DbSet.FirstOrDefaultAsync(x => x.Email == email, token);
    }

    public async Task<User?> GetUserByPublicIdAsync(Guid publicId, CancellationToken token, bool isTracking = true)
    {
        if (isTracking)
            return await DbSet.Include(u => u.CartItems)
                .Include(u => u.UserVouchers)
                .FirstOrDefaultAsync(x => x.PublicId == publicId, token);
        return await DbSet.FirstOrDefaultAsync(x => x.PublicId == publicId, token);
    }

    public IQueryable<User> GetAllUsers()
    {
        return DbSet.AsNoTracking();
    }

    public async Task<bool> CheckUserExistsAsync(Guid publicId, CancellationToken token)
    {
        return await DbSet.AsNoTracking().AnyAsync(x => x.PublicId == publicId, token);
    }

    public async Task<int?> GetUserIdByPublicIdAsync(Guid publicId, CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .Where(x => x.PublicId == publicId)
            .Select(x => x.Id)
            .FirstOrDefaultAsync(token);
    }

    public async Task<string?> GetUserDefaultAddressAsync(Guid userId, CancellationToken token)
    {
        return await DbSet.AsNoTracking()
            .Where(u => u.PublicId == userId)
            .SelectMany(u => u.UserAddresses)
            .Where(a => a.IsDefault)
            .Select(a => a.Address)
            .FirstOrDefaultAsync(token);
    }
}