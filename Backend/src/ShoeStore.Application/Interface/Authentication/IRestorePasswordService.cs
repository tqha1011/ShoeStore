using ErrorOr;

namespace ShoeStore.Application.Interface.Authentication;

public interface IRestorePasswordService
{
    public Task<ErrorOr<Success>> SendRestorePasswordEmailAsync(string email, CancellationToken token);
    
    public Task<ErrorOr<Success>> VerifyOtpAsync(string email,string otpCode, CancellationToken token);
    
    public Task<ErrorOr<Success>> UpdatePasswordAsync(string email, string otp, string newPassword,
        CancellationToken token);
}