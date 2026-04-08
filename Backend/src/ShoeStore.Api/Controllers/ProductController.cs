using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface.ProductInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for retrieving product catalog information for frontend users.
///     Provides endpoints for searching products, filtering, and retrieving product details.
///     No authorization required for public product browsing.
/// </summary>
/// <param name="productService">Service for handling product operations.</param>
[ApiController]
[Route("api/[controller]")]
// [Authorize(Roles = "User")]
public class ProductsController(IProductService productService) : ControllerBase
{
    /// <summary>
    ///     Searches and retrieves products with pagination, filtering, and sorting options.
    /// </summary>
    /// <remarks>
    ///     Supports query parameters for:
    ///     - <c>searchTerm</c>: search keyword to find products by name or description
    ///     - <c>categoryId</c>: filter products by specific category
    ///     - <c>minPrice</c>: filter products with minimum price
    ///     - <c>maxPrice</c>: filter products with maximum price
    ///     - <c>pageNumber</c>: page number for pagination (default: 1)
    ///     - <c>pageSize</c>: number of results per page (default: 10)
    ///     - <c>sortBy</c>: sort results by field (e.g., price, rating, newest)
    ///     Returns paginated results with total count for frontend pagination controls.
    /// </remarks>
    /// <param name="request">Search request containing filter and pagination parameters.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Products retrieved successfully. Returns paginated list of products.</response>
    /// <response code="400">Bad request; invalid search parameters provided.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>
    ///     An action result containing paginated product list on success, or an error response describing what went
    ///     wrong.
    /// </returns>
    [HttpGet("search")]
    public async Task<IActionResult> Search([FromQuery] ProductSearchRequest request, CancellationToken token)
    {
        var results = await productService.GetProductsAsync(request, token);

        return results.Match<IActionResult>(
            pageResult => Ok(pageResult),
            errors => BadRequest(new
            {
                message = "Failed to search products",
                description = errors[0].Description
            })
        );
    }


    /// <summary>
    ///     Retrieves detailed information about a specific product by its identifier.
    /// </summary>
    /// <remarks>
    ///     Returns comprehensive product details including:
    ///     - Product name, description, and pricing
    ///     - Product variants (sizes, colors) with individual pricing and stock status
    ///     - Product images and specifications
    ///     - Category information and related metadata
    ///     This endpoint is useful for displaying product detail pages on the frontend.
    /// </remarks>
    /// <param name="productGuid">The unique identifier (GUID) of the product to retrieve.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Product details retrieved successfully. Returns complete product information.</response>
    /// <response code="404">Not found; the product with the specified ID does not exist.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>
    ///     An action result containing detailed product information on success, or an error response describing what went
    ///     wrong.
    /// </returns>
    [HttpGet("{productGuid}")]
    public async Task<IActionResult> ShowDetail(Guid productGuid, CancellationToken token)
    {
        var result = await productService.GetProductByGuidAsync(productGuid, token);

        var response = result.Match<IActionResult>(
            product => Ok(new
            {
                message = "Get product detail successfully",
                data = product
            }),
            errors => errors[0].Code switch
            {
                "Product.NotFound" => NotFound(new
                {
                    message = "Product not found",
                    description = errors[0].Description
                }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = "An unexpected error occurred.Please try again later",
                    description = errors[0].Description
                })
            }
        );
        return response;
    }
}