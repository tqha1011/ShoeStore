using System.Text;
using Microsoft.AspNetCore.Authentication.JwtBearer;
using Microsoft.IdentityModel.Tokens;
using ShoeStore.Application.DependencyInjection;
using ShoeStore.Infrastructure.DependencyInjection;
using Scalar.AspNetCore;
using ShoeStore.Api.JsonSerialize;
using ShoeStore.Api.Middlewares;
using DotNetEnv;

Env.TraversePath().Load(); // load environment variables from .env file
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
        IssuerSigningKey = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(builder.Configuration["JWT_KEY"]!)),
        ClockSkew = TimeSpan.Zero // set clock skew to zero to prevent token expiration issues
    };
});

builder.Services.AddProblemDetails(); // return 
builder.Services.AddExceptionHandler<GlobalExceptionHandler>(); // register global exception handler middleware
builder.Services.AddApplicationServices(builder.Configuration);
builder.Services.AddInfrastructure(builder.Configuration);
var app = builder.Build();

app.UseExceptionHandler(); // use GlobalExceptionHandler middleware to handle exceptions globally
app.MapOpenApi();
app.MapScalarApiReference();
app.UseAuthentication();
app.UseAuthorization();
app.MapControllers();
app.Run();

