using ShoeStore.Application.DTOs;

namespace ShoeStore.Application.Interface;

public interface IMasterData
{
    Task<List<SizeDto>> GetSizesAsync(CancellationToken token);

    Task<List<ColorDto>> GetColorsAsync(CancellationToken token);
    
    Task<List<CategoryDto>> GetCategoriesAsync(CancellationToken token);
}