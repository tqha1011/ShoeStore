using System.IdentityModel.Tokens.Jwt;
using System.Security.Claims;
using System.Text;
using Microsoft.Extensions.Configuration;
using Microsoft.IdentityModel.Tokens;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Infrastructure.Authentication;

public class TokenService(IConfiguration configuration) : ITokenService
{
    public string GenerateToken(Guid userPublicId, string email, UserRole role)
    {
        // Gets variables from .env
        var secretKey = configuration["JWT_KEY"] ?? throw new InvalidOperationException("JWT_KEY is missing");
        var audience = configuration["JWT_AUDIENCE"];
        var issuer = configuration["JWT_ISSUER"];
        
        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(secretKey));
        var claims = new List<Claim>
        {
            new(ClaimTypes.NameIdentifier, userPublicId.ToString()),
            new(ClaimTypes.Email, email),
            new(ClaimTypes.Role, role.ToString())
        };
        
        // Create secret signing credentials
        var credentials = new SigningCredentials(key, SecurityAlgorithms.HmacSha256Signature);
        
        // Describe what is in the token
        var tokenDescriptor = new SecurityTokenDescriptor()
        {
            Subject = new ClaimsIdentity(claims),
            Expires = DateTime.UtcNow.AddHours(1),
            Audience = audience,
            Issuer = issuer,
            SigningCredentials = credentials
        };

        var tokenHandler = new JwtSecurityTokenHandler();
        var token = tokenHandler.CreateToken(tokenDescriptor);
        return tokenHandler.WriteToken(token);
    }
}