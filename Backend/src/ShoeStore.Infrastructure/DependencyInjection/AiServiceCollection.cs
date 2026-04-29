using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.SemanticKernel;

namespace ShoeStore.Infrastructure.DependencyInjection;

public static class AiServiceCollection
{
    public static IServiceCollection AddChatBotInfrastructure(this IServiceCollection services,IConfiguration configuration)
    {
        var apiKey = configuration["Chatbot:ApiKey"] ?? throw new InvalidOperationException("Chatbot API key is missing");
        var model = configuration["Chatbot:Model"] ?? throw new InvalidOperationException("Chatbot model is missing");
        var url = configuration["Chatbot:Url"] ??  throw new InvalidOperationException("Chatbot url is missing");
        
        var endpoint = new Uri(url);
        var builder = Kernel.CreateBuilder();

        builder.AddOpenAIChatCompletion(
            modelId: model,
            apiKey: apiKey,
            endpoint: endpoint
        );
        
        // register Singleton because kernel is thread-safe
        services.AddSingleton(builder.Build());
        return services;
    }
}