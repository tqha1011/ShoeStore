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

        var embeddingModel = configuration[$"Chatbot:{provider}:EmbeddingModel"] ??
                             throw new InvalidOperationException("Chatbot embedding model is missing");
        
        var summaryModel = configuration[$"Chatbot:{provider}:SmallModel"] ??
                           throw new InvalidOperationException("Chatbot summary model is missing");

        if (provider == "Ollama")
        {
            var url = configuration[$"Chatbot:{provider}:Url"] ??
                      throw new InvalidOperationException("Chatbot url is missing");

            var endpoint = new Uri(url);

            var openAiClient = new OpenAIClient(new ApiKeyCredential(apiKey), new OpenAIClientOptions
            {
                Endpoint = endpoint
            });

            services.AddOpenAIChatCompletion(
                model.Trim(),
                openAiClient);

            services.AddOpenAIChatCompletion(
                summaryModel.Trim(),
                openAiClient,
                "summary"
            );

            services.AddOpenAIEmbeddingGenerator(embeddingModel, openAiClient);
        }
        else
        {
            services.AddGoogleAIGeminiChatCompletion(
                model.Trim(),
                apiKey
            );

            services.AddGoogleAIEmbeddingGenerator(
                embeddingModel.Trim(),
                apiKey
            );
        }

        services.AddKernel();
        return services;
    }
}