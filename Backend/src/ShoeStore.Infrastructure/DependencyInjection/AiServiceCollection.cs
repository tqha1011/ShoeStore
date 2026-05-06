using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.SemanticKernel;

namespace ShoeStore.Infrastructure.DependencyInjection;

public static class AiServiceCollection
{
    public static IServiceCollection AddChatBotInfrastructure(this IServiceCollection services,
        IConfiguration configuration)
    {
        var provider = configuration["Chatbot:ActiveProvider"] ?? "Gemini";
        var apiKey = configuration[$"Chatbot:{provider}:ApiKey"] ??
                     throw new InvalidOperationException("Chatbot API key is missing");
        var model = configuration[$"Chatbot:{provider}:Model"] ??
                    throw new InvalidOperationException("Chatbot model is missing");
        if (provider == "Ollama")
        {
            var url = configuration[$"Chatbot:{provider}:Url"] ??
                      throw new InvalidOperationException("Chatbot url is missing");

            var endpoint = new Uri(url);

            services.AddOpenAIChatCompletion(
                model,
                apiKey: apiKey,
                endpoint: endpoint
            );
        }
        else
        {
            services.AddGoogleAIGeminiChatCompletion(
                model,
                apiKey
            );
        }

        services.AddKernel();
        return services;
    }
}