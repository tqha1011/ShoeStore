using System.ComponentModel;
using System.Globalization;
using Microsoft.Extensions.Logging;
using Microsoft.SemanticKernel;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.MasterDataInterface;
using ShoeStore.Domain.Entities;
using Color = ShoeStore.Domain.Entities.Color;

namespace ShoeStore.Application.Plugin;

public class MasterDataPluginService(
    IColorRepository colorRepository,
    ISizeRepository sizeRepository,
    IUnitOfWork unitOfWork,
    ICurrentUser currentUser,
    ILogger<MasterDataPluginService> logger) : IMasterDataPluginService
{
    // LLM entry point: create a new color in master data after explicit user approval.
    [KernelFunction("add-new-color")]
    [Description("Creates a new product color in the master data database. " +
                 "CRITICAL: Call this function ONLY if the user explicitly agrees to create a new color after getting a 'ColorNotFound' status. " +
                 "After calling, analyze the 'Status' field: " +
                 "1. If 'Success': The color is created, proceed to retry the user's previous action (like adding a variant). " +
                 "2. If 'UserNotValid': Apologize and inform the user that they do not have the required admin permissions." +
                 "3. If 'InvalidColorName': Inform the user to provide a color name again." +
                 "4. If 'ColorAlreadyExists': Apologize and remind the user that the color already exists. Please choose a different color name.")]
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

        if (string.IsNullOrWhiteSpace(colorName))
            return new MasterDataResultDto("InvalidColorName", "Invalid color name");

        var normalizeColorName = CultureInfo.CurrentCulture.TextInfo.ToTitleCase(colorName.Trim().ToLower());

        var isColorExist = await colorRepository.ColorNameExistAsync(normalizeColorName, token);
        if (isColorExist)
        {
            logger.LogWarning("The color already exists: {ColorName}", normalizeColorName);
            return new MasterDataResultDto("ColorAlreadyExists", "The color already exists");
        }

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
                 "3. If 'InvalidSize': Apologize and remind the user that the size must be a valid number between 0 and 50." +
                 "4. If 'SizeAlreadyExists': Inform user that the size already exists and ask them to provide a different size number.")]
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

        var isSizeExist = await sizeRepository.ProductSizeExistsAsync(size, token);
        if (isSizeExist)
        {
            logger.LogWarning("The product size already exists: {Size}", size);
            return new MasterDataResultDto("SizeAlreadyExists", "The product size already exists");
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