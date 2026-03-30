using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface.ProductInterface;

namespace ShoeStore.API.Controllers;

/// <summary>
///     Admin controller for managing products, including CRUD operations and search functionality.
/// </summary>
/// <param name="productService"></param>
[Route("api/admin/products")]
[ApiController]
[Authorize(Roles = "Admin")]
public class AdminProductController(IProductService productService) : ControllerBase
{
    /// <summary>
    ///     Search and filter products with pagination
    /// </summary>
    [HttpGet("search")]
    public async Task<IActionResult> Search([FromQuery] ProductSearchRequest request, CancellationToken token)
    {
        var results = await productService.GetProductsAsync(request, token);

        if (results.IsError)
            return BadRequest(results.Errors);

        return Ok(results.Value);
    }

    /// <summary>
    ///     Get product details by ID
    /// </summary>
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
    ///     Create a new product
    /// </summary>
    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateProductDto productDto, CancellationToken token)
    {
        var result = await productService.AddProductAsync(productDto, token);

        if (result.IsError)
            return BadRequest(result.Errors);

        return CreatedAtAction(nameof(GetByGuid), new { productGuid = result.Value }, null);
    }

    /// <summary>
    ///     Update an existing product
    /// </summary>
    [HttpPut("{productGuid:guid}")]
    public async Task<IActionResult> Update(Guid productGuid, [FromBody] UpdateProductDto productDto,
        CancellationToken token)
    {
        var result = await productService.UpdateProductAsync(productGuid, productDto, token);

        var response = result.Match<IActionResult>(
            updated => Ok(updated),
            errors => Problem(
                string.Join(", ", errors.Select(e => e.Description)),
                statusCode: StatusCodes.Status400BadRequest
            )
        );
        return response;
    }

    /// <summary>
    ///     Delete a product by ID
    /// </summary>
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