using DotNetEnv;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;
using Npgsql;

namespace ShoeStore.Infrastructure.Data;

public class AppDbContextFactory : IDesignTimeDbContextFactory<AppDbContext>
{
    public AppDbContext CreateDbContext(string[] args)
    {
        // Automatically load environment variables from .env file
        Env.TraversePath().Load();

        var connectionString = Environment.GetEnvironmentVariable("ConnectionStrings__DefaultConnection");

        if (string.IsNullOrEmpty(connectionString))
            throw new InvalidOperationException(
                "Connection string 'DEFAULT_CONNECTION' not found in environment variables.");
        
        var vectorBuilder = new NpgsqlDataSourceBuilder(connectionString);
        vectorBuilder.UseVector();
        var dataSource = vectorBuilder.Build();

        // Configure DbContextOptions với Npgsql và retry logic
        var builder = new DbContextOptionsBuilder<AppDbContext>();
        builder.UseNpgsql(dataSource,
                opt => opt
                    .UseVector()
                    .EnableRetryOnFailure(
                        5,
                        TimeSpan.FromSeconds(30),
                        null))
            .UseSnakeCaseNamingConvention();

        return new AppDbContext(builder.Options);
    }
}