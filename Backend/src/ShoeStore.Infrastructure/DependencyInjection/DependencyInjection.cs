using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using ShoeStore.Application.Interface;
using ShoeStore.Infrastructure.Repositories;

namespace ShoeStore.Infrastructure.DependencyInjection;

public static class DependencyInjection
{
    public static IServiceCollection AddInfrastructure(this IServiceCollection services, IConfiguration configuration)
    {
        // register dependency injection in infrastructure layer
        services.AddScoped<IUnitOfWork, UnitOfWork>();
        services.AddScoped(typeof(IGenericRepository<,>), typeof(GenericRepository<,>));
        return services;
    }
}