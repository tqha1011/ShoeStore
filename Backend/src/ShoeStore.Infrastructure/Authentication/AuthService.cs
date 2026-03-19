using ShoeStore.Application.Interface;
using ErrorOr;
using ShoeStore.Application.DTOs.AuthDTOs;
using ShoeStore.Domain.Entities;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Infrastructure.Authentication;

public class AuthService(
    IPasswordHash passwordHash,
    IUserRepository userRepository,
    IUnitOfWork unitOfWork,
    ITokenService tokenService) : IAuthService
{
    public async Task<ErrorOr<Created>> RegisterAsync(RegisterDto registerDto, CancellationToken token)
    {
        var email = registerDto.Email;
        var password = registerDto.Password;
        var res = await userRepository.IsEmailExistAsync(email, token);
        if (res)
        {
            return Error.Failure("Email.Exist","Email already exists");
        }
        
        var passHashed = passwordHash.HashPassword(password);
        var user = new User
        {
            Email = email,
            Password = passHashed,
            CreatedAt = DateTime.UtcNow,
            UpdatedAt = DateTime.UtcNow,
            UserName = email.Split('@')[0],
            Role = UserRole.Customer
        };
        userRepository.Add(user);
        await unitOfWork.SaveChangesAsync(token);
        return Result.Created;
    }

    public async Task<ErrorOr<string>> LoginAsync(LoginDto loginDto, CancellationToken token)
    {
        var correctUser = await userRepository.GetUserByEmailAsync(loginDto.Email, token);
        
        if (correctUser == null || !passwordHash.VerifyPassword(loginDto.Password,correctUser.Password))
        {
            return Error.Unauthorized("Invalid.Credential", "Email or password is incorrect");
        }
        
        // Generate JWT token
        var jwtToken = tokenService.GenerateToken(correctUser.Id, correctUser.Email, correctUser.Role);
        return jwtToken;
    }
}