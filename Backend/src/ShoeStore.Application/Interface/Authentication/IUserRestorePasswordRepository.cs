using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface.Authentication;

public interface IUserRestorePasswordRepository : IGenericRepository<UserRestorePassword,int>
{
    public Task<bool> IsValidOtpAsync(string email,string otp, CancellationToken token);
    Task<UserRestorePassword?> GetValidOtpAsync(string email, string otp, CancellationToken token);
}