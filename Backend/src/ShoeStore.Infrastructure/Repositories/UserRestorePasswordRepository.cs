using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class UserRestorePasswordRepository(AppDbContext context)
    : GenericRepository<UserRestorePassword, int>(context), IUserRestorePasswordRepository
{
    public async Task<bool> IsValidOtpAsync(string email, string otp, CancellationToken token)
    {
        return await DbSet.AnyAsync(x => x.Token == otp
                                         && !x.IsUsed
                                         && x.Expiration > DateTime.UtcNow
                                         && x.User.Email == email, token);
    }

    public async Task<UserRestorePassword?> GetValidOtpAsync(string email, string otp, CancellationToken token)
    {
        return await DbSet.Include(x => x.User)
            .FirstOrDefaultAsync(x => x.Token == otp
                                      && !x.IsUsed
                                      && x.Expiration > DateTime.UtcNow
                                      && x.User.Email == email, token);
    }
}