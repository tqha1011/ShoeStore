using System.ComponentModel;
using System.Globalization;
using Microsoft.Extensions.Logging;
using Microsoft.SemanticKernel;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.MasterDataInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Plugin;

public class MasterDataPluginService(
    IColorRepository colorRepository,
    ISizeRepository sizeRepository,
    IUnitOfWork unitOfWork,
    ICurrentUser currentUser,
    ILogger<MasterDataPluginService> logger)
{
    // LLM entry point: create a new color in master data after explicit user approval.
    [KernelFunction("add-new-color")]
    [Description("Creates a new product color in the master data database. " +
                 "CRITICAL: Call this function ONLY if the user explicitly agrees to create a new color after getting a 'ColorNotFound' status. " +
                 "After calling, analyze the 'Status' field: " +
                 "1. If 'Success': The color is created, proceed to retry the user's previous action (like adding a variant). " +
                 "2. If 'UserNotValid': Apologize and inform the user that they do not have the required admin permissions.")]
    public async Task<MasterDataResultDto> AddNewColor(
        [Description("The name of the new color to create (e.g., 'Đỏ', 'Xanh lá').")]
        string colorName,
        CancellationToken token)
    {
        if (currentUser.Id == null || !currentUser.IsAdmin)
        {
            logger.LogError("Unauthorized access attempt to add-new-color by user {UserId}", currentUser.Id);
            return new MasterDataResultDto("UserNotValid", "Unauthorized user");
        }

        var normalizeColorName = CultureInfo.CurrentCulture.TextInfo.ToTitleCase(colorName.Trim().ToLower());
        var newColor = new Color
        {
            ColorName = normalizeColorName
        };
        colorRepository.Add(newColor);
        await unitOfWork.SaveChangesAsync(token);
        return new MasterDataResultDto("Success", "Add new color successfully");
    }

    // LLM entry point: create a new size in master data after explicit user approval.
    [KernelFunction("add-new-size")]
    [Description("Creates a new product size in the master data database. " +
                 "CRITICAL: Call this function ONLY if the user explicitly agrees to create a new size after getting a 'SizeNotFound' status. " +
                 "After calling, analyze the 'Status' field: " +
                 "1. If 'Success': The size is created, proceed to retry the user's previous action (like adding a variant). " +
                 "2. If 'UserNotValid': Apologize and inform the user that they do not have the required admin permissions. " +
                 "3. If 'InvalidSize': Apologize and remind the user that the size must be a valid number between 0 and 50.")]
    public async Task<MasterDataResultDto> AddNewSize(
        [Description("The numeric shoe size to create (e.g., 40, 42, 45.5).")]
        decimal size, CancellationToken token)
    {
        if (currentUser.Id == null || !currentUser.IsAdmin)
        {
            logger.LogError("Unauthorized access attempt to add-new-size by user {UserId}", currentUser.Id);
            return new MasterDataResultDto("UserNotValid", "Unauthorized user");
        }

        if (size is < 0 or > 50)
        {
            logger.LogWarning("Invalid size value {Size}", size);
            return new MasterDataResultDto("InvalidSize", "Size must be between 0 and 50");
        }

        var newSize = new ProductSize
        {
            Size = size
        };
        sizeRepository.Add(newSize);
        await unitOfWork.SaveChangesAsync(token);
        return new MasterDataResultDto("Success", "Add new size successfully");
    }
}