using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
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

    public async Task<User?> GetUserByPublicIdAsync(Guid publicId, CancellationToken token)
    {
        return await DbSet.FirstOrDefaultAsync(x => x.PublicId == publicId, token);
    }
}