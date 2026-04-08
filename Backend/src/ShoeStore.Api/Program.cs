using System.Security.Claims;
using System.Text;
using System.Threading.RateLimiting;
using FluentValidation.AspNetCore;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.AspNetCore.Mvc.Infrastructure;
using Microsoft.AspNetCore.RateLimiting;
using Microsoft.IdentityModel.Tokens;
using Scalar.AspNetCore;
using ShoeStore.Api.JsonSerialize;
using ShoeStore.Api.Middlewares;
using ShoeStore.Application.DependencyInjection;
using ShoeStore.Infrastructure.Cloudinary;
using ShoeStore.Infrastructure.DependencyInjection;
var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi

builder.Services.AddOpenApi();

builder.Services.AddControllers()
    .AddJsonOptions(options =>
    {
        // force .NET not to use Reflection
        // Use source generator to generate serialization code at compile time, which can improve performance and reduce memory usage
        options.JsonSerializerOptions.TypeInfoResolverChain.Insert(0, AppJsonSerializeContext.Default);
    });

builder.Services.AddHttpClient();
builder.Services.Configure<CloudinarySettings>(builder.Configuration.GetSection("Cloudinary"));
var jwtKey = builder.Configuration["Jwt__Key"];
builder.Services.AddAuthentication(options =>
{
    options.DefaultAuthenticateScheme = JwtBearerDefaults.AuthenticationScheme;
    options.DefaultChallengeScheme = JwtBearerDefaults.AuthenticationScheme;
}).AddJwtBearer(options =>
{
    options.TokenValidationParameters = new TokenValidationParameters
    {
        ValidateIssuer = true,
        ValidateAudience = true,
        ValidateLifetime = true,
        ValidateIssuerSigningKey = true,
        ValidIssuer = builder.Configuration["JWT_ISSUER"],
        ValidAudience = builder.Configuration["JWT_AUDIENCE"],
        IssuerSigningKey = new SymmetricSecurityKey(
            Encoding.UTF8.GetBytes(jwtKey ?? throw new InvalidOperationException("JWT_KEY is null"))
        ),
        ClockSkew = TimeSpan.Zero // set clock skew to zero to prevent token expiration issues
    };
});

builder.Services.AddProblemDetails(); // return 
builder.Services.AddFluentValidationAutoValidation();
builder.Services.AddExceptionHandler<GlobalExceptionHandler>(); // register global exception handler middleware
builder.Services.AddApplicationServices(builder.Configuration);
builder.Services.AddInfrastructure(builder.Configuration);

// Add rate-limit to protect API
builder.Services.AddRateLimiter(options =>
{
    // return 429 Too Many Requests when the client exceeds the rate limit
    options.RejectionStatusCode = StatusCodes.Status429TooManyRequests;
    options.OnRejected = async (context, cancellationToken) =>
    {
        if (context.Lease.TryGetMetadata(MetadataName.RetryAfter, out var retryAfter))
        {
            context.HttpContext.Response.Headers.RetryAfter = $"{retryAfter.TotalSeconds}";
            var problemDetailsFactory = context.HttpContext.RequestServices
                                            .GetService<ProblemDetailsFactory>() ??
                                        throw new InvalidOperationException(
                                            "ProblemDetailsFactory is null");
            var problemDetails = problemDetailsFactory
                .CreateProblemDetails(
                    context.HttpContext,
                    StatusCodes.Status429TooManyRequests,
                    "Too many requests",
                    detail: $"Please retry after {retryAfter.TotalSeconds} seconds");

            await context.HttpContext.Response.WriteAsJsonAsync(problemDetails, cancellationToken);
        }
    };
    // this is for heavy-load api
    options.AddConcurrencyLimiter("concurrency", option =>
    {
        option.PermitLimit = 20;
        option.QueueProcessingOrder = QueueProcessingOrder.OldestFirst;
        option.QueueLimit = 0;
    });

    options.AddPolicy("limit-per-user", httpContext =>
    {
        var userId = httpContext.User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (!string.IsNullOrEmpty(userId))
            return RateLimitPartition.GetTokenBucketLimiter(
                userId,
                _ => new TokenBucketRateLimiterOptions
                {
                    TokenLimit = 10,
                    TokensPerPeriod = 3,
                    ReplenishmentPeriod = TimeSpan.FromMinutes(1)
                });

        return RateLimitPartition.GetFixedWindowLimiter(
            httpContext.Connection.RemoteIpAddress?.ToString() ?? "anonymous",
            _ => new FixedWindowRateLimiterOptions
            {
                PermitLimit = 10,
                Window = TimeSpan.FromMinutes(1),
                QueueProcessingOrder = QueueProcessingOrder.OldestFirst,
                QueueLimit = 0
            });
    });
});
var app = builder.Build();

app.UseExceptionHandler(); // use GlobalExceptionHandler middleware to handle exceptions globally
app.MapOpenApi();
app.MapScalarApiReference();
app.UseAuthentication();
app.UseAuthorization();
app.UseRateLimiter();
app.MapControllers();
app.Run();