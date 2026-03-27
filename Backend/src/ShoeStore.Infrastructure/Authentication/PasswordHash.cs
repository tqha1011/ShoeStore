using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;

namespace ShoeStore.Infrastructure.Authentication;

public class PasswordHasher : IPasswordHash
{
    public string HashPassword(string password)
    {
        return BCrypt.Net.BCrypt.HashPassword(password);
    }

    public bool VerifyPassword(string password, string hashedPassword)
    {
        return BCrypt.Net.BCrypt.Verify(password, hashedPassword);
    }
}