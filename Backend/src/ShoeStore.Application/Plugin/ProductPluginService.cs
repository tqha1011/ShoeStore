using System.ComponentModel;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using Microsoft.SemanticKernel;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.Hub;
using ShoeStore.Application.Interface.MasterDataInterface;
using ShoeStore.Application.Interface.ProductInterface;

namespace ShoeStore.Application.Plugin;

public class ProductPluginService(
    ISizeRepository sizeRepository,
    IColorRepository colorRepository,
    IProductRepository productRepository,
    ICurrentUser currentUser,
    INotifyBotResponse notifyBotResponse,
    ILogger<ProductPluginService> logger) : IProductPluginService
{
    // LLM entry point: search product by keyword, enforce admin check, return a single/multiple/not-found status.
    [KernelFunction("search-product")]
    [Description("Searches for products in the database using a keyword or product name. " +
                 "CRITICAL INSTRUCTION: You MUST use this function FIRST to find and confirm the exact product before adding any variants. " +
                 "After calling this function, check the 'Status' field in the result: " +
                 "1. If Status is 'NotFound': Stop and ask the user to provide a different name. " +
                 "2. If Status is 'MultipleFound': Stop and list the found products to the user, asking them to choose the correct one. " +
                 "3. If Status is 'UserNotValid': Apologize and inform the user that they do not have the required admin permissions to perform this action." +
                 "4. If Status is 'Success': You have found the exact product, proceed with the user's next request using the provided PublicId.")]
    public async Task<SearchResultDto> SearchProduct(
        [Description(
            "The specific name, brand, or keyword of the product the user wants to find (e.g., 'Nike Air Force 1').")]
        string keyword,
        CancellationToken token)
    {
        if (currentUser.Id == null || !currentUser.IsAdmin)
        {
            logger.LogError("Unauthorized access attempt to search-product by user {UserId}", currentUser.Id);
            return new SearchResultDto("UserNotValid", "User is not valid", []);
        }

        var productKeywords = new ProductSearchRequest
        {
            Keyword = keyword
        };
        var products = await productRepository.SearchProduct(productKeywords)
            .Select(p => new ProductResponseDto
            {
                PublicId = p.PublicId,
                ProductName = p.ProductName,
                Brand = p.Brand ?? "Nike",
                CategoryId = p.CategoryId,
                CategoryName = p.Category!.Name,
                Variants = p.ProductVariants.Where(v => v.IsSelling && !v.IsDeleted)
                    .Select(v => new ProductVariantResponseDto
                    {
                        PublicId = v.PublicId,
                        SizeId = v.SizeId,
                        Size = v.Size!.Size,
                        ColorId = v.ColorId,
                        ColorName = v.Color!.ColorName,
                        Price = v.Price
                    }).ToList()
            })
            .Take(20)
            .ToListAsync(token);
        var response = products.Count switch
        {
            0 => new SearchResultDto("NotFound", "Can not find matching product", products),
            > 1 => new SearchResultDto("MultipleFound", "Find more than 1 product, please provide clearly keyword",
                products),
            _ => new SearchResultDto("Success", "Find matching product", products)
        };
        await notifyBotResponse.NotifyProductSearchResultAsync(response, currentUser.Id.Value);
        return response;
    }

    // LLM entry point: validate inputs, resolve size/color/product, then notify admin UI via SignalR.
    [KernelFunction("add-product-variant-draft")]
    [Description("Validates and prepares a draft for a new product variant. " +
                 "CRITICAL: You MUST have the exact product GUID before calling this function. " +
                 "After calling, analyze the 'Status' field in the response: " +
                 "1. If 'Success': Tell the user that the draft has been created successfully on their screen. " +
                 "2. If 'SizeNotFound' or 'ColorNotFound': Apologize, explain that the requested size or color does not exist in the master data, and PROACTIVELY ASK the user if they want you to automatically create that new size or color for them. " +
                 "3. If 'InvalidStock' or 'InvalidPrice': Ask the user to provide a positive number for stock/price. " +
                 "4. If 'ProductNotFound': Inform the user that the product is invalid and ask them to search again." +
                 "5. If 'UserNotValid': Apologize and inform the user that they do not have the required admin permissions to perform this action." +
                 "6. If 'VariantAlreadyExist': Inform the user that a variant with the same size and color already exists for this product, and ask them to provide different attributes.")]
    public async Task<AddVariantResultDto> AddNewVariant(
        [Description("The unique GUID of the parent product (obtained from the search-product function).")]
        Guid publicProductId,
        [Description("The shoe size number (e.g., 40, 41, 42).")]
        decimal size,
        [Description("The name of the color (e.g., 'Trắng', 'Đen', 'Xanh').")]
        string colorName,
        [Description("The quantity of shoes in stock (must be >= 0).")]
        int stock,
        [Description("The selling price of the variant (must be >= 0).")]
        decimal price,
        [Description("The URL of the variant's image. Leave null if the user does not provide one.")]
        string? imageUrl, CancellationToken token)
    {
        if (stock < 0) return new AddVariantResultDto("InvalidStock", "Stock cannot be negative", null);
        if (price < 0) return new AddVariantResultDto("InvalidPrice", "Price cannot be negative", null);
        if (currentUser.Id == null || !currentUser.IsAdmin)
        {
            logger.LogError("Unauthorized access attempt to search-product by user {UserId}", currentUser.Id);
            return new AddVariantResultDto("UserNotValid", "User is not valid", null);
        }

        var draftData = new VariantResultDto(
            Guid.Empty, // productPublicId placeholder
            0, // sizeId placeholder,
            size,
            0, // colorId placeholder
            colorName,
            stock, price, imageUrl);

        var product = await productRepository.GetDetailsByGuidAsync(publicProductId, token);
        if (product == null)
        {
            logger.LogWarning("Product {ProductId} not found", publicProductId);
            await notifyBotResponse.NotifyAddVariantDraftAsync(
                new AddVariantResultDto("ProductNotFound", $"Product {publicProductId} not found", draftData),
                currentUser.Id.Value);
            return new AddVariantResultDto("ProductNotFound", $"Product {publicProductId} not found", null);
        }

        draftData = draftData with { ProductId = publicProductId };

        var sizeId = await sizeRepository.GetProductSizesIdAsync(size, token);
        if (sizeId == null)
        {
            logger.LogWarning("Invalid size {Size}", size);
            await notifyBotResponse.NotifyAddVariantDraftAsync(
                new AddVariantResultDto("SizeNotFound", $"Size {size} does not exist", draftData),
                currentUser.Id.Value);
            return new AddVariantResultDto("SizeNotFound", $"Size {size} does not exist", null);
        }

        draftData = draftData with { SizeId = sizeId.Value };

        var colorId = await colorRepository.GetColorIdAsync(colorName, token);
        if (colorId == null)
        {
            logger.LogWarning("Invalid color {Color}", colorName);
            await notifyBotResponse.NotifyAddVariantDraftAsync(
                new AddVariantResultDto("ColorNotFound", $"Color {colorName} does not exist", draftData),
                currentUser.Id.Value);
            return new AddVariantResultDto("ColorNotFound", $"Color {colorName} does not exist", null);
        }

        draftData = draftData with { ColorId = colorId.Value };

        if (product!.ProductVariants.Any(v =>
                v is { IsSelling: true, IsDeleted: false } && v.ColorId == colorId.Value && v.SizeId == sizeId.Value))
        {
            logger.LogWarning("Variant with {Size} and {Color} is exist", size, colorName);
            return new AddVariantResultDto("VariantAlreadyExist",
                $"Variant with size {size} and color {colorName} already exists", null);
        }

        var response = new AddVariantResultDto("Success", "All information is correct", draftData);
        await notifyBotResponse.NotifyAddVariantDraftAsync(response, currentUser.Id.Value);
        return response;
    }
}