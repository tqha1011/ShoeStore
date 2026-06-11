using System.ClientModel;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.SemanticKernel;
using OpenAI;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Plugin;

namespace ShoeStore.Infrastructure.DependencyInjection;

public static class AiServiceCollection
{
    public static IServiceCollection AddChatBotInfrastructure(this IServiceCollection services,
        IConfiguration configuration)
    {
        var provider = configuration["Chatbot:ActiveProvider"];
        provider = string.IsNullOrWhiteSpace(provider) ? "Gemini" : provider.Trim();

        var apiKey = GetRequiredConfigurationValue(configuration, $"Chatbot:{provider}:ApiKey",
            "Chatbot API key is missing");
        var model = GetRequiredConfigurationValue(configuration, $"Chatbot:{provider}:Model",
            "Chatbot model is missing");
        var embeddingModel = GetRequiredConfigurationValue(configuration, $"Chatbot:{provider}:EmbeddingModel",
            "Chatbot embedding model is missing");

        var summaryModel = configuration[$"Chatbot:{provider}:SmallModel"];
        summaryModel = string.IsNullOrWhiteSpace(summaryModel) ? model : summaryModel.Trim();

        if (provider.Equals("Ollama", StringComparison.OrdinalIgnoreCase))
        {
            var url = GetRequiredConfigurationValue(configuration, $"Chatbot:{provider}:Url",
                "Chatbot url is missing");

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

            services.AddOpenAIEmbeddingGenerator(embeddingModel.Trim(), openAiClient);
        }
        else
        {
            services.AddGoogleAIGeminiChatCompletion(
                model.Trim(),
                apiKey
            );

            services.AddGoogleAIGeminiChatCompletion(
                summaryModel.Trim(),
                apiKey,
                serviceId: "summary"
            );

            services.AddGoogleAIEmbeddingGenerator(
                embeddingModel.Trim(),
                apiKey
            );
        }

        services.AddScoped<IProductPluginService, ProductPluginService>();
        services.AddScoped<IMasterDataPluginService, MasterDataPluginService>();
        services.AddScoped<IStoreAssistantPluginService, StoreAssistantPluginService>();
        services.AddScoped<IInvoicePluginService, InvoicePluginService>();
        services.AddKernel();
        return services;
    }

    private static string GetRequiredConfigurationValue(IConfiguration configuration, string key, string message)
    {
        var value = configuration[key];
        if (string.IsNullOrWhiteSpace(value))
            throw new InvalidOperationException(message);

        return value.Trim();
    }
}
