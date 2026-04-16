using ErrorOr;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.MasterDataDTOs;
using ShoeStore.Application.Interface.MasterDataInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Provides master data used by frontend filters and product forms.
/// </summary>
/// <param name="masterDataService">Service used to load sizes, colors, and categories.</param>
[Route("api/master-data")]
[Authorize]
[ApiController]
public class MasterDataController(IMasterDataService masterDataService) : ControllerBase
{
    /// <summary>
    ///     Gets all available sizes.
    /// </summary>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Sizes returned successfully.</response>
    /// <response code="500">Failed to load sizes.</response>
    [ProducesResponseType(typeof(List<SizeDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("sizes")]
    public async Task<IActionResult> GetSizes(CancellationToken token)
    {
        var sizesResult = await masterDataService.GetSizesAsync(token);
        var response = sizesResult.Match<IActionResult>(
            sizes => Ok(sizes),
            errors => StatusCode(StatusCodes.Status500InternalServerError, new
            {
                message = "Failed to load sizes",
                detail = errors[0].Description
            }));

        return response;
    }

    /// <summary>
    ///     Gets all available colors.
    /// </summary>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Colors returned successfully.</response>
    /// <response code="500">Failed to load colors.</response>
    [ProducesResponseType(typeof(List<ColorDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("colors")]
    public async Task<IActionResult> GetColors(CancellationToken token)
    {
        var colorsResult = await masterDataService.GetColorsAsync(token);
        var response = colorsResult.Match<IActionResult>(
            colors => Ok(colors),
            errors => StatusCode(StatusCodes.Status500InternalServerError, new
            {
                message = "Failed to load colors",
                detail = errors[0].Description
            }));

        return response;
    }

    /// <summary>
    ///     Gets all available categories.
    /// </summary>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Categories returned successfully.</response>
    /// <response code="500">Failed to load categories.</response>
    [ProducesResponseType(typeof(List<CategoryDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("categories")]
    public async Task<IActionResult> GetCategories(CancellationToken token)
    {
        var categoriesResult = await masterDataService.GetCategoriesAsync(token);
        var response = categoriesResult.Match<IActionResult>(
            categories => Ok(categories),
            errors => StatusCode(StatusCodes.Status500InternalServerError, new
            {
                message = "Failed to load categories",
                detail = errors[0].Description
            }));

        return response;
    }

    /// <summary>
    ///     Adds a new size and returns the updated size list.
    /// </summary>
    /// <param name="request">Payload containing the size value.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Size added successfully.</response>
    /// <response code="409">Conflict; size already exists.</response>
    /// <response code="500">Failed to add size.</response>
    [ProducesResponseType(typeof(List<SizeDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status409Conflict)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost("sizes")]
    [Authorize(Roles = "Admin")]
    public async Task<IActionResult> AddSize([FromBody] AddSizeDto request, CancellationToken token)
    {
        var result = await masterDataService.AddSizeAsync(request.Size, token);
        var response = result.Match<IActionResult>(
            sizes => Ok(sizes),
            errors => errors[0].Type switch
            {
                ErrorType.Conflict => Conflict(new
                {
                    message = "Size already exists",
                    detail = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "Failed to add size",
                    detail = errors[0].Description
                })
            });

        return response;
    }

    /// <summary>
    ///     Adds a new color and returns the updated color list.
    /// </summary>
    /// <param name="request">Payload containing the color name.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Color added successfully.</response>
    /// <response code="409">Conflict; color already exists.</response>
    /// <response code="500">Failed to add color.</response>
    [ProducesResponseType(typeof(List<ColorDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status409Conflict)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost("colors")]
    [Authorize(Roles = "Admin")]
    public async Task<IActionResult> AddColor([FromBody] AddColorDto request, CancellationToken token)
    {
        var result = await masterDataService.AddColorAsync(request.ColorName, token);
        var response = result.Match<IActionResult>(
            colors => Ok(colors),
            errors => errors[0].Type switch
            {
                ErrorType.Conflict => Conflict(new
                {
                    message = "Color already exists",
                    detail = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "Failed to add color",
                    detail = errors[0].Description
                })
            });

        return response;
    }

    /// <summary>
    ///     Adds a new category and returns the updated category list.
    /// </summary>
    /// <param name="request">Payload containing the category name.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Category added successfully.</response>
    /// <response code="409">Conflict; category already exists.</response>
    /// <response code="500">Failed to add category.</response>
    [ProducesResponseType(typeof(List<CategoryDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status409Conflict)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost("categories")]
    [Authorize(Roles = "Admin")]
    public async Task<IActionResult> AddCategory([FromBody] AddCategoryDto request, CancellationToken token)
    {
        var result = await masterDataService.AddCategoryAsync(request.CategoryName, token);
        var response = result.Match<IActionResult>(
            categories => Ok(categories),
            errors => errors[0].Type switch
            {
                ErrorType.Conflict => Conflict(new
                {
                    message = "Category already exists",
                    detail = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "Failed to add category",
                    detail = errors[0].Description
                })
            });

        return response;
    }
}