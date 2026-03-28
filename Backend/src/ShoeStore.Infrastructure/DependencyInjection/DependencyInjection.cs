using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.Notification;
using ShoeStore.Application.Interface.Strategies;
using ShoeStore.Application.Interface.Upload_Image;
using ShoeStore.Application.Services;
using ShoeStore.Application.Services;
using ShoeStore.Infrastructure.Authentication;
using ShoeStore.Infrastructure.Authentication;
using ShoeStore.Infrastructure.Authentication.Strategies;
using ShoeStore.Infrastructure.Cloundinary;
using ShoeStore.Infrastructure.Data;
using ShoeStore.Infrastructure.Notification;
using ShoeStore.Infrastructure.Repositories;
using ShoeStore.Infrastructure.RestorePassService;

namespace ShoeStore.Infrastructure.DependencyInjection;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        services.Configure<CloudinarySettings>(configuration.GetSection("Cloudinary"));
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
        services.AddScoped<IUserRepository,UserRepository>();
        services.AddScoped<IAuthService,AuthService>();
        services.AddScoped<IProductRepository,ProductRepository>();
        services.AddScoped<IProductService, ProductService>();
        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<IAuthService, AuthService>();
        services.AddScoped<IEmailService, EmailService>();
        services.AddScoped<IRestorePasswordService, RestorePasswordService>();
        services.AddScoped<IUserRestorePasswordRepository, UserRestorePasswordRepository>();
        services.AddKeyedScoped<ISocialAuthStrategy, GoogleAuthStrategy>("Google");
        services.AddKeyedScoped<ISocialAuthStrategy, FacebookAuthStrategy>("Facebook");
        services.AddScoped<IImageService, CloudinaryService>();
        return services;
    }
}