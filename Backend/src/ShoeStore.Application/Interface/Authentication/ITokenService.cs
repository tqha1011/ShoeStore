using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Interface.Authentication;

public interface ITokenService
{
    string GenerateToken(Guid userPublicId, string email, UserRole role);
}