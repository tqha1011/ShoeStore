using ShoeStore.Application.DependencyInjection;
using ShoeStore.Infrastructure.DependencyInjection;
using Scalar.AspNetCore;

var builder = WebApplication.CreateBuilder(args);

// Add services to the container.
// Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
builder.Services.AddOpenApi();

builder.Services.AddControllers();

builder.Services.AddApplicationServices(builder.Configuration);
builder.Services.AddInfrastructure(builder.Configuration);
var app = builder.Build();

app.MapOpenApi();
app.MapScalarApiReference();

// app.UseHttpsRedirection();
app.MapControllers();
app.Run();

