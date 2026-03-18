using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Interface;

public interface ITokenService
{
    string GenerateToken(int userId, string email, UserRole role);
}