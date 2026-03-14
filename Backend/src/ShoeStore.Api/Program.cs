using ShoeStore.Application.DependencyInjection;
using ShoeStore.Infrastructure.DependencyInjection;
using Scalar.AspNetCore;
using ShoeStore.Api.JsonSerialize;
using ShoeStore.Api.Middlewares;

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

builder.Services.AddProblemDetails(); // return 
builder.Services.AddExceptionHandler<GlobalExceptionHandler>(); // register global exception handler middleware
builder.Services.AddApplicationServices(builder.Configuration);
builder.Services.AddInfrastructure(builder.Configuration);
var app = builder.Build();

app.UseExceptionHandler(); // use GlobalExceptionHandler middleware to handle exceptions globally
app.MapOpenApi();
app.MapScalarApiReference();
app.MapControllers();
app.Run();

