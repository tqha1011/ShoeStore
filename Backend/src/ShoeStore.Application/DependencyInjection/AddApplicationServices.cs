using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;


namespace ShoeStore.Application.DependencyInjection;

public static class DependencyInjection
{
    public static IServiceCollection AddApplicationServices(this IServiceCollection services,IConfiguration configuration)
    {
        // register dependency injection in application layer
        return services;
    }
}