using System.ClientModel;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.SemanticKernel;
using OpenAI;

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
            
            var embeddingModel = configuration[$"Chatbot:{provider}:EmbeddingModel"] ??
                                 throw new InvalidOperationException("Chatbot embedding model is missing");

            var endpoint = new Uri(url);

            var openAiClient = new OpenAIClient(new ApiKeyCredential(apiKey), new OpenAIClientOptions
            {
                Endpoint = endpoint
            });

            services.AddOpenAIChatCompletion(
                model,
                openAiClient);

            services.AddOpenAIEmbeddingGenerator(embeddingModel, openAiClient);
        }
        else
        {
            var embeddingModel = configuration[$"Chatbot:{provider}:EmbeddingModel"] ??
                                 throw new InvalidOperationException("Chatbot embedding model is missing");
            
            services.AddGoogleAIGeminiChatCompletion(
                model,
                apiKey
            );

            services.AddGoogleAIEmbeddingGenerator(
                embeddingModel,
                apiKey
            );
        }

        services.AddKernel();
        return services;
    }
}