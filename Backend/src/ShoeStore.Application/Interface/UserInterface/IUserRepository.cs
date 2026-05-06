using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.UserInterface;

public interface IUserRepository : IGenericRepository<User, int>
{
    Task<bool> IsEmailExistAsync(string email, CancellationToken token);

    Task<User?> GetUserByEmailAsync(string email, CancellationToken token);

    Task<User?> GetUserByPublicIdAsync(Guid publicId, CancellationToken token, bool isTracking = true);
    IQueryable<User> GetAllUsers();
    Task<bool> CheckUserExistsAsync(Guid publicId, CancellationToken token);
}