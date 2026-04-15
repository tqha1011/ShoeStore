using Microsoft.Extensions.Caching.Hybrid;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.Interface;

namespace ShoeStore.Application.Services;

public class MasterData(
    IColorRepository colorRepository, 
    ISizeRepository sizeRepository, 
    HybridCache cache,
    ICategoryRepository categoryRepository)
    : IMasterData
{
    public async Task<List<SizeDto>> GetSizesAsync(CancellationToken token)
    {
        var cachedSizes = await cache.GetOrCreateAsync("master_data_sizes", async cancel =>
        {
            var sizeList = await sizeRepository.GetProductSizesAsync(cancel);
            return sizeList.Select(x => new SizeDto(x.Id, x.Size)).ToList();
        }, cancellationToken: token);
        return cachedSizes;
    }

    public async Task<List<ColorDto>> GetColorsAsync(CancellationToken token)
    {
        var cachedColors = await cache.GetOrCreateAsync("master_data_colors", async cancel =>
        {
            var colorList = await colorRepository.GetColorsAsync(cancel);
            return colorList.Select(x => new ColorDto(x.Id, x.ColorName)).ToList();
        }, cancellationToken: token);
        return cachedColors;
    }

    public async Task<List<CategoryDto>> GetCategoriesAsync(CancellationToken token)
    {
        var cachedCategory = await cache.GetOrCreateAsync("master_data_categories", async cancel =>
        {
            var categoryList = await categoryRepository.GetCategoriesAsync(cancel);
            return categoryList.Select(x => new CategoryDto(x.Id, x.Name)).ToList();
        }, cancellationToken: token);
        
        return cachedCategory;
    }
}