using ErrorOr;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface.ProductInterface;

namespace ShoeStore.API.Controllers;

/// <summary>
///     Admin controller for managing the product catalog with full CRUD operations.
///     Provides endpoints for creating, reading, updating, and deleting products.
///     All operations require Admin role authorization.
/// </summary>
/// <param name="productService">Service for handling product management operations.</param>
[Route("api/admin/products")]
[ApiController]
[Authorize(Roles = "Admin")]
public class AdminProductController(IProductService productService) : ControllerBase
{
    /// <summary>
    ///     Searches and filters products with pagination support (Admin view).
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization and supports query parameters for:
    ///     - <c>searchTerm</c>: search keyword to find products by name or description
    ///     - <c>categoryId</c>: filter products by specific category
    ///     - <c>minPrice</c>: filter products with minimum price
    ///     - <c>maxPrice</c>: filter products with maximum price
    ///     - <c>pageNumber</c>: page number for pagination (default: 1)
    ///     - <c>pageSize</c>: number of results per page (default: 10)
    ///     - <c>sortBy</c>: sort results by field (e.g., price, rating, newest)
    ///     Returns complete product information including stock levels and detailed variant info.
    /// </remarks>
    /// <param name="request">Search request containing filter and pagination parameters.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Products retrieved successfully. Returns paginated list of products with complete details.</response>
    /// <response code="400">Bad request; invalid search parameters provided.</response>
    /// <response code="401">Unauthorized; user must have Admin role authorization.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>
    ///     An action result containing paginated product list on success, or an error response describing what went
    ///     wrong.
    /// </returns>
    [ProducesResponseType(typeof(PageResult<ProductResponseDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("search")]
    public async Task<IActionResult> Search([FromQuery] ProductAdminRequestDto request, CancellationToken token)
    {
        var results = await productService.GetProductsAdminAsync(request, token);

        if (results.IsError)
            return BadRequest(results.Errors);

        return Ok(results.Value);
    }

    /// <summary>
    ///     Retrieves detailed information about a specific product by its identifier (Admin view).
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization and returns comprehensive product information including:
    ///     - Product name, description, and pricing
    ///     - All product variants with detailed stock information
    ///     - Category information and timestamps
    ///     - Complete product metadata for editing
    /// </remarks>
    /// <param name="productGuid">The unique identifier (GUID) of the product to retrieve.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Product details retrieved successfully. Returns complete product information.</response>
    /// <response code="404">Not found; the product with the specified ID does not exist.</response>
    /// <response code="401">Unauthorized; user must have Admin role authorization.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>
    ///     An action result containing detailed product information on success, or an error response describing what went
    ///     wrong.
    /// </returns>
    [ProducesResponseType(typeof(ProductResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpGet("{productGuid:guid}")]
    public async Task<IActionResult> GetByGuid(Guid productGuid, CancellationToken token)
    {
        // Get product details by ID
        var result = await productService.GetProductByGuidAsync(productGuid, token);

        if (result.IsError)
            return NotFound(result.Errors);

        return Ok(result.Value);
    }

    /// <summary>
    ///     Creates a new product in the catalog.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization and a request body with:
    ///     - <c>name</c>: the product name/title
    ///     - <c>description</c>: detailed product description
    ///     - <c>categoryId</c>: the category identifier for the product
    ///     - <c>basePrice</c>: the base price for the product
    ///     After creation, variants can be added separately using the product variant endpoints.
    /// </remarks>
    /// <param name="productDto">The product details to create.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="201">Product created successfully. Returns the created product location and details.</response>
    /// <response code="400">Bad request; invalid product data provided.</response>
    /// <response code="401">Unauthorized; user must have Admin role authorization.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>An action result with status 201 (Created) on success, or an error response describing what went wrong.</returns>
    [ProducesResponseType(StatusCodes.Status201Created)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateProductDto productDto, CancellationToken token)
    {
        var result = await productService.AddProductAsync(productDto, token);

        return result.Match<IActionResult>(
            publicId => CreatedAtAction(nameof(GetByGuid), new { productGuid = publicId }, new
            {
                message = "Product created successfully",
                data = publicId
            }),
            errors => BadRequest(new
            {
                message = "Failed to create product",
                errors = errors.Select(e => e.Description)
            })
        );
    }

    /// <summary>
    ///     Updates an existing product's information.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization and a request body with:
    ///     - <c>name</c>: the updated product name
    ///     - <c>description</c>: the updated product description
    ///     - <c>categoryId</c>: the updated category identifier
    ///     - <c>basePrice</c>: the updated base price
    ///     This endpoint updates the product master information and handles multiple variants (Size + Colors).
    /// </remarks>
    /// <param name="productGuid">The unique identifier of the product to update.</param>
    /// <param name="productDto">The updated product details.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Product updated successfully. Returns the updated product details.</response>
    /// <response code="400">Bad request; invalid product data provided.</response>
    /// <response code="404">Not found; the product with the specified ID does not exist.</response>
    /// <response code="401">Unauthorized; user must have Admin role authorization.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>
    ///     An action result containing the updated product details on success, or an error response describing what went
    ///     wrong.
    /// </returns>
    [ProducesResponseType(typeof(Updated), StatusCodes.Status200OK)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpPut("{productGuid:guid}")]
    public async Task<IActionResult> Update(Guid productGuid, [FromBody] UpdateProductDto productDto,
        CancellationToken token)
    {
        var result = await productService.UpdateProductAsync(productGuid, productDto, token);

        return result.Match<IActionResult>(
            updated => Ok(new
            {
                message = "Product and variants updated successfully",
                data = updated
            }),
           errors => errors[0].Code switch
           {
               "Product.NotFound" => NotFound(new { message = "Update failed", description = errors[0].Description }),
               _ => BadRequest(new
               {
            message = "Failed to update product",
            errors = errors.Select(e => e.Description)
               })
           }
        );
    }

    /// <summary>
    ///     Deletes a product from the catalog.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization.
    ///     Deleting a product will also cascade delete all associated variants and related data.
    ///     This operation is irreversible, so verify deletion is intended before confirming.
    /// </remarks>
    /// <param name="productGuid">The unique identifier of the product to delete.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="204">Product deleted successfully. Returns no content.</response>
    /// <response code="404">Not found; the product with the specified ID does not exist.</response>
    /// <response code="401">Unauthorized; user must have Admin role authorization.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>An action result with status 204 (No Content) on success, or an error response describing what went wrong.</returns>
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType(typeof(object), StatusCodes.Status400BadRequest)]
    [ProducesResponseType(typeof(object), StatusCodes.Status401Unauthorized)]
    [ProducesResponseType(typeof(object), StatusCodes.Status404NotFound)]
    [ProducesResponseType(typeof(object), StatusCodes.Status500InternalServerError)]
    [HttpDelete("{productGuid:guid}")]
    public async Task<IActionResult> Delete(Guid productGuid, CancellationToken token)
    {
        var result = await productService.DeleteProductAsync(productGuid, token);

        if (result.IsError)
        {
            if (result.FirstError.Code == "Product.NotFound")
                return NotFound(result.Errors);

            return BadRequest(result.Errors);
        }

        return NoContent();
    }
}