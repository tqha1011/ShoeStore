using System.Reflection;
using FluentValidation;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using ShoeStore.Application.Interface.Authentication;
using ShoeStore.Application.Interface.ProductInterface;
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
        return services;
    }
}