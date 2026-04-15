using ErrorOr;
using ShoeStore.Application.DTOs.MasterDataDTOs;

namespace ShoeStore.Application.Interface;

public interface IMasterDataService
{
    Task<ErrorOr<List<SizeDto>>> GetSizesAsync(CancellationToken token);

    Task<ErrorOr<List<ColorDto>>> GetColorsAsync(CancellationToken token);

    Task<ErrorOr<List<CategoryDto>>> GetCategoriesAsync(CancellationToken token);

    Task<ErrorOr<List<SizeDto>>> AddSizeAsync(decimal size, CancellationToken token);

    Task<ErrorOr<List<ColorDto>>> AddColorAsync(string colorName, CancellationToken token);

    Task<ErrorOr<List<CategoryDto>>> AddCategoryAsync(string categoryName, CancellationToken token);
}