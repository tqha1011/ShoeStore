using System.Text.RegularExpressions;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Microsoft.Extensions.Logging;
using Microsoft.SemanticKernel;
using Microsoft.SemanticKernel.ChatCompletion;
using ShoeStore.Application.Constants;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;


namespace ShoeStore.Infrastructure.Worker;

public partial class UpdateTitleSessionService(
    IServiceScopeFactory scopeFactory,
    ILogger<UpdateTitleSessionService> logger,
    IUpdateTitleQueue queue,
    Kernel kernel) : BackgroundService
{
    [GeneratedRegex(@"##\s*🚀\s*CAMPAIGN:\s*(.+)", RegexOptions.IgnoreCase | RegexOptions.Compiled)]
    private static partial Regex CampaignTitleRegex();
    
    protected override async Task ExecuteAsync(CancellationToken stoppingToken)
    {
        logger.LogInformation("Starting update title session service");
        while (!stoppingToken.IsCancellationRequested)
            try
            {
                var requestDto = await queue.DequeueAsync(stoppingToken);
                using var scope = scopeFactory.CreateScope();
                var sessionRepository = scope.ServiceProvider.GetRequiredService<IChatSessionRepository>();
                var unitOfWork = scope.ServiceProvider.GetRequiredService<IUnitOfWork>();
                var bot = kernel.Services.GetRequiredKeyedService<IChatCompletionService>("summary");
                var session = await sessionRepository.GetChatSessionByPublicIdAsync(requestDto.PublicSessionId, requestDto.UserId, stoppingToken);
                if (session != null)
                {
                    string newTitle;
                    if (requestDto.IsGenerateCampaign)
                    {
                        newTitle = ExtractCampaignTitle(session.Title);
                    }
                    else
                    {
                        var botResponse = await GenerateTitleAsync(bot, requestDto, stoppingToken);
                        newTitle = botResponse.Content ?? $"Chat {DateTime.Now:dd/MM}";
                    }
                    session.Title = newTitle;
                    await unitOfWork.SaveChangesAsync(stoppingToken);
                    logger.LogInformation($"Update title session {session.PublicId} completed");
                }
            }
            catch (Exception ex)
            {
                logger.LogError(ex, "Error in update title session service");
            }
    }

    private static async Task<ChatMessageContent> GenerateTitleAsync(IChatCompletionService bot,
        UpdateTitleRequestDto requestDto,CancellationToken cancellationToken)
    {
        var systemPrompt = SystemPrompt.GenerateCreateTitlePrompt();
        var chat = new ChatHistory(systemPrompt);
        chat.AddUserMessage(requestDto.Content);
        
        return await bot.GetChatMessageContentAsync(chat, cancellationToken: cancellationToken);
    }

    private static string ExtractCampaignTitle(string botResponse)
    {
        var match = CampaignTitleRegex().Match(botResponse);

        return match.Success ? match.Groups[1].Value.Trim() : $"🚀 New campaign - {DateTime.Now:dd/MM}";
    }
}