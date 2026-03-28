using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface;

public interface IUserRepository : IGenericRepository<User,int>
{
    Task<bool> IsEmailExistAsync(string email,CancellationToken token);
    
    Task<User?> GetUserByEmailAsync(string email, CancellationToken token);
    
    Task<User?> GetUserByPublicIdAsync(Guid publicId, CancellationToken token);
}