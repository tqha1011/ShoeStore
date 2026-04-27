using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.CheckoutInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Interface.MasterDataInterface;
using ShoeStore.Application.Interface.Notification;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Interface.Strategies;
using ShoeStore.Application.Interface.UploadImage;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Application.Services;
using ShoeStore.Infrastructure.Authentication;
using ShoeStore.Infrastructure.Authentication.Strategies;
using ShoeStore.Infrastructure.Cloudinary;
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

        services.AddHealthChecks().AddDbContextCheck<AppDbContext>("/api/health");
        services.AddScoped<IPasswordHash, PasswordHasher>();
        services.AddScoped<ITokenService, TokenService>();
        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<IProductRepository, ProductRepository>();
        services.AddScoped<IUserRepository, UserRepository>();
        services.AddScoped<IEmailService, EmailService>();
        services.AddScoped<IRestorePasswordService, RestorePasswordService>();
        services.AddScoped<IUserRestorePasswordRepository, UserRestorePasswordRepository>();
        services.AddKeyedScoped<ISocialAuthStrategy, GoogleAuthStrategy>("Google");
        services.AddKeyedScoped<ISocialAuthStrategy, FacebookAuthStrategy>("Facebook");
        services.AddScoped<IImageService, CloudinaryService>();
        services.AddScoped<IProductVariantRepository, ProductVariantRepository>();
        services.AddScoped<ICartItemRepository, CartItemRepository>();
        services.AddHttpContextAccessor();
        services.AddScoped<ICurrentUser, CurrentUser>();
        services.AddScoped<IInvoiceRepository, InvoiceRepository>();
        services.AddScoped<IInvoiceService, InvoiceService>();
        services.AddScoped<IPaymentTransactionRepository, PaymentTransactionRepository>();
        services.AddScoped<IPaymentRepository, PaymentRepository>();
        services.AddScoped<IColorRepository, ColorRepository>();
        services.AddScoped<ISizeRepository, SizeRepository>();
        services.AddScoped<ICategoryRepository, CategoryRepository>();
        services.AddScoped<IVoucherRepository, VoucherRepository>();
        services.AddScoped<IVoucherService, VoucherService>();
        services.AddScoped<IUserVoucherRepository, UserVoucherRepository>();
        services.AddScoped<IGoogleValidator, GoogleValidator>();
        services.AddSingleton<INotificationQueue, NotificationQueue>();
        return services;
    }
}