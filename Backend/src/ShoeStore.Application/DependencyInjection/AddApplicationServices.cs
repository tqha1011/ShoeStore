using System.Reflection;
using FluentValidation;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using ShoeStore.Application.Validations.AuthValidation;


namespace ShoeStore.Application.DependencyInjection;

public static class DependencyInjection
{
    public static IServiceCollection AddApplicationServices(this IServiceCollection services,IConfiguration configuration)
    {
        // register dependency injection in application layer
        services.AddValidatorsFromAssembly(Assembly.GetExecutingAssembly());
        return services;
    }
}