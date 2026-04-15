using ErrorOr;
using Microsoft.Extensions.Caching.Hybrid;
using ShoeStore.Application.DTOs.MasterDataDTOs;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.MasterDataInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class MasterDataService(
    IColorRepository colorRepository,
    ISizeRepository sizeRepository,
    HybridCache cache,
    ICategoryRepository categoryRepository,
    IUnitOfWork unitOfWork)
    : IMasterDataService
{
    public async Task<ErrorOr<List<SizeDto>>> GetSizesAsync(CancellationToken token)
    {
        var cachedSizes = await cache.GetOrCreateAsync("master_data_sizes", async cancel =>
        {
            var sizeList = await sizeRepository.GetProductSizesAsync(cancel);
            return sizeList.Select(x => new SizeDto(x.Id, x.Size)).ToList();
        }, cancellationToken: token);
        return cachedSizes;
    }

    public async Task<ErrorOr<List<ColorDto>>> GetColorsAsync(CancellationToken token)
    {
        var cachedColors = await cache.GetOrCreateAsync("master_data_colors", async cancel =>
        {
            var colorList = await colorRepository.GetColorsAsync(cancel);
            return colorList.Select(x => new ColorDto(x.Id, x.ColorName)).ToList();
        }, cancellationToken: token);
        return cachedColors;
    }

    public async Task<ErrorOr<List<CategoryDto>>> GetCategoriesAsync(CancellationToken token)
    {
        var cachedCategory = await cache.GetOrCreateAsync("master_data_categories", async cancel =>
        {
            var categoryList = await categoryRepository.GetCategoriesAsync(cancel);
            return categoryList.Select(x => new CategoryDto(x.Id, x.Name)).ToList();
        }, cancellationToken: token);

        return cachedCategory;
    }

    public async Task<ErrorOr<List<SizeDto>>> AddSizeAsync(decimal size, CancellationToken token)
    {
        var existSize = await sizeRepository.ProductSizeExistsAsync(size, token);
        if (existSize) return Error.Conflict("Size.Conflict", "Size already exists");
        var newSize = new ProductSize
        {
            Size = size
        };
        sizeRepository.Add(newSize);
        await unitOfWork.SaveChangesAsync(token);
        await cache.RemoveAsync("master_data_sizes", token);
        var result = await GetSizesAsync(token);
        return result;
    }

    public async Task<ErrorOr<List<ColorDto>>> AddColorAsync(string colorName, CancellationToken token)
    {
        var existColor = await colorRepository.ColorNameExistAsync(colorName, token);
        if (existColor) return Error.Conflict("ColorName.Conflict", "Color already exists");
        var newColor = new Color
        {
            ColorName = colorName
        };
        colorRepository.Add(newColor);
        await unitOfWork.SaveChangesAsync(token);
        await cache.RemoveAsync("master_data_colors", token);
        var result = await GetColorsAsync(token);
        return result;
    }

    public async Task<ErrorOr<List<CategoryDto>>> AddCategoryAsync(string categoryName, CancellationToken token)
    {
        var existCategory = await categoryRepository.CategoryNameExistAsync(categoryName, token);
        if (existCategory) return Error.Conflict("CategoryName.Conflict", "Category already exists");
        var newCategory = new Category
        {
            Name = categoryName
        };
        categoryRepository.Add(newCategory);
        await unitOfWork.SaveChangesAsync(token);
        await cache.RemoveAsync("master_data_categories", token);
        var result = await GetCategoriesAsync(token);
        return result;
    }
}