using System.Reflection;
using FluentValidation;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.CartItemInterface;
using ShoeStore.Application.Interface.CheckoutInterface;
using ShoeStore.Application.Interface.MasterDataInterface;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Interface.StatisticsInterface;
using ShoeStore.Application.Interface.VoucherInterface;
using ShoeStore.Application.Services;

namespace ShoeStore.Application.DependencyInjection;

public static class DependencyInjection
{
    public static IServiceCollection AddApplicationServices(this IServiceCollection services,
        IConfiguration configuration)
    {
        // register dependency injection in application layer
        services.AddValidatorsFromAssembly(Assembly.GetExecutingAssembly());
        services.AddScoped<IAuthService, AuthService>();
        services.AddScoped<IProductService, ProductService>();
        services.AddScoped<IProductVariantService, ProductVariantService>();
        services.AddScoped<ICartItemService, CartItemService>();
        services.AddScoped<ICheckOutService, CheckOutService>();
        services.AddScoped<IPaymentService, PaymentService>();
        services.AddScoped<IStatisticsService, StatisticsService>();
        services.AddScoped<IMasterDataService, MasterDataService>();
        services.AddScoped<IUserVoucherService,UserVoucherService>();
        services.AddScoped<IChatBotService,ChatBotService>();
        return services;
    }
}