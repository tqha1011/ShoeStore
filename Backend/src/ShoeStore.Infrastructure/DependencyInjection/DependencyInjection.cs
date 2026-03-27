using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.Notification;
using ShoeStore.Infrastructure.Authentication;
using ShoeStore.Infrastructure.Data;
using ShoeStore.Infrastructure.Notification;
using ShoeStore.Infrastructure.Repositories;
using ShoeStore.Infrastructure.RestorePassService;

namespace ShoeStore.Infrastructure.DependencyInjection;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        var connectionString = configuration.GetConnectionString("DefaultConnection");
        // register dependency injection in infrastructure layer
        services.AddScoped<IUnitOfWork, UnitOfWork>();
        services.AddDbContext<AppDbContext>(options =>
            options.UseNpgsql(connectionString,
                opt
                    => opt.EnableRetryOnFailure(
                        5,
                        TimeSpan.FromSeconds(30),
                        null)).UseSnakeCaseNamingConvention());

        services.AddHealthChecks().AddDbContextCheck<AppDbContext>();
        services.AddScoped<IPasswordHash, PasswordHasher>();
        services.AddScoped<ITokenService, TokenService>();
        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<IAuthService, AuthService>();
        services.AddScoped<IEmailService, EmailService>();
        services.AddScoped<IRestorePasswordService, RestorePasswordService>();
        services.AddScoped<IUserRestorePasswordRepository, UserRestorePasswordRepository>();
        return services;
    }
}