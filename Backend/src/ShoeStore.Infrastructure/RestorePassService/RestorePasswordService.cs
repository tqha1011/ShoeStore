using System.Security.Cryptography;
using ErrorOr;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Infrastructure.RestorePassService;

public class RestorePasswordService(
    IUserRepository userRepository,
    IUserRestorePasswordRepository userRestorePasswordRepository,
    IUnitOfWork unitOfWork,
    IEmailService emailService,
    IPasswordHash passwordHash) : IRestorePasswordService
{
    /// <summary>
    ///     It uses Mailkit to send otp to the user's email
    ///     Generate otp and save it in the database with the user's id and an expiration time of 15 minutes
    /// </summary>
    /// <param name="email"></param>
    /// <param name="token"></param>
    /// <returns></returns>
    public async Task<ErrorOr<Success>> SendRestorePasswordEmailAsync(string email, CancellationToken token)
    {
        var correctUser = await userRepository.GetUserByEmailAsync(email, token);
        if (correctUser == null) return Error.Unauthorized("Email.Invalid", "Email is not registered");
        var secureOtp = RandomNumberGenerator.GetInt32(100000, 1000000).ToString();
        var expiredTime = DateTime.UtcNow.AddMinutes(15);

        var emailBodyTemplate =
            """
            <div style="font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e0e0e0; border-radius: 10px; background-color: #ffffff; box-shadow: 0 4px 6px rgba(0,0,0,0.05);">
                <div style="text-align: center; padding-bottom: 20px; border-bottom: 2px solid #f0f0f0;">
                    <h2 style="color: #2c3e50; margin: 0; font-size: 24px;">ShoeStore API</h2>
                </div>
                <div style="padding: 30px 0; color: #444; line-height: 1.6; font-size: 16px;">
                    <p>Hello there,</p>
                    <p>We received a request to reset the password for your account. Here is your One-Time Password (OTP) to proceed:</p>
                    
                    <div style="text-align: center; margin: 35px 0;">
                        <span style="display: inline-block; font-size: 36px; font-weight: bold; color: #1a73e8; background-color: #f0f4f9; padding: 15px 30px; border-radius: 8px; letter-spacing: 8px; border: 1px dashed #1a73e8;">{{OTP_CODE}}</span>
                    </div>
                    
                    <p style="color: #e74c3c; font-size: 14px; text-align: center; font-weight: bold;">
                        ⚠️ This code will expire in 15 minutes. Please do not share this code with anyone!
                    </p>
                    <p style="font-size: 14px; text-align: center; color: #777;">
                        If you did not request a password reset, you can safely ignore this email. Your account remains secure.
                    </p>
                </div>
                <div style="text-align: center; padding-top: 20px; border-top: 2px solid #f0f0f0; color: #999; font-size: 12px;">
                    <p>&copy; 2026 ShoeStore. All rights reserved.</p>
                </div>
            </div>
            """;

        var finalEmailBody = emailBodyTemplate.Replace("{{OTP_CODE}}", secureOtp);
        var restorePassword = new UserRestorePassword
        {
            Token = secureOtp,
            UserId = correctUser.Id,
            Expiration = expiredTime
        };
        userRestorePasswordRepository.Add(restorePassword);
        await unitOfWork.SaveChangesAsync(token);
        await emailService.SendEmailAsync("tranquangha2006@gmail.com", email, "Restore password",
            finalEmailBody, token);
        return Result.Success;
    }

    public async Task<ErrorOr<Success>> VerifyOtpAsync(string email, string otpCode, CancellationToken token)
    {
        var response = await userRestorePasswordRepository.IsValidOtpAsync(email, otpCode, token);
        if (!response) return Error.Unauthorized("OTP.Invalid", "Invalid OTP code or OTP code has expired");
        return Result.Success;
    }

    public async Task<ErrorOr<Success>> UpdatePasswordAsync(string email, string otp, string newPassword,
        CancellationToken token)
    {
        // prevent hacker using postman call directively api 
        var response = await userRestorePasswordRepository.GetValidOtpAsync(email, otp, token);
        if (response == null) return Error.Unauthorized("OTP.Invalid", "Invalid OTP code or OTP code has expired");

        var newPassHash = passwordHash.HashPassword(newPassword);
        response.User.Password = newPassHash;
        response.IsUsed = true;
        await unitOfWork.SaveChangesAsync(token);
        return Result.Success;
    }
}