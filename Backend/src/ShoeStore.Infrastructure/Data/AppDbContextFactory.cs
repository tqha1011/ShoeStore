using DotNetEnv;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace ShoeStore.Infrastructure.Data;

public class AppDbContextFactory : IDesignTimeDbContextFactory<AppDbContext>
{
    public AppDbContext CreateDbContext(string[] args)
    {
        // Automatically load environment variables from .env file
        Env.TraversePath().Load();
        
        var connectionString = Environment.GetEnvironmentVariable("DEFAULT_CONNECTION");

        if (string.IsNullOrEmpty(connectionString))
        {
            throw new InvalidOperationException("Connection string 'DEFAULT_CONNECTION' not found in environment variables.");
        }

        // Configure DbContextOptions với Npgsql và retry logic
        var builder = new DbContextOptionsBuilder<AppDbContext>();
        builder.UseNpgsql(connectionString, 
                npgsqlOptionsAction: opt => opt.EnableRetryOnFailure(
                    maxRetryCount: 5,
                    maxRetryDelay: TimeSpan.FromSeconds(30),
                    errorCodesToAdd: null))
            .UseSnakeCaseNamingConvention();

        return new AppDbContext(builder.Options);
    }
}