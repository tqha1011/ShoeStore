using System.Text;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using ShoeStore.Application.Interface;
using ShoeStore.Infrastructure.Data;
using ShoeStore.Infrastructure.Repositories;
using Microsoft.EntityFrameworkCore;
using Microsoft.IdentityModel.Tokens;
using ShoeStore.Infrastructure.Authentication;

namespace ShoeStore.Infrastructure.DependencyInjection;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        var connectionString = configuration["DEFAULT_CONNECTION"];
        // register dependency injection in infrastructure layer
        services.AddScoped<IUnitOfWork, UnitOfWork>();
        services.AddDbContext<AppDbContext>(options => 
            options.UseNpgsql(connectionString, 
                npgsqlOptionsAction: opt 
                    => opt.EnableRetryOnFailure(
                        maxRetryCount: 5,
                        maxRetryDelay: TimeSpan.FromSeconds(30),
                        errorCodesToAdd: null)).UseSnakeCaseNamingConvention());
        
        services.AddHealthChecks().AddDbContextCheck<AppDbContext>();
        services.AddScoped<IPasswordHash, PasswordHasher>();
        services.AddScoped<ITokenService, TokenService>();
        services.AddScoped<IUserRepository,UserRepository>();
        return services;
    }
}