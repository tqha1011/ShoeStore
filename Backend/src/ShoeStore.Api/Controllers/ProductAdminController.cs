using Microsoft.AspNetCore.Mvc;

namespace ShoeStore.API.Controllers;

/// <summary>
/// Controller for administrative product operations such as Creating, Updating, and Deleting products.
/// Restricted to users with the 'Admin' role.
/// </summary>
[Route("api/admin/products")]
[ApiController]
[Authorize(Roles = "Admin")] // Chỉ Admin mới có quyền truy cập vào Controller này
public class AdminProductController(IProductService productService) : ControllerBase
{
    /// <summary>
    /// Create a new product.
    /// </summary>
    [HttpPost]
    public async Task<IActionResult> Create([FromBody] ProductCreateDto createDto, CancellationToken token)
    {
        var result = await productService.CreateProductAsync(createDto, token);

        return result.Match<IActionResult>(
            product => CreatedAtAction("GetById", "Products", new { id = product.Id }, product),
            errors => BadRequest(new
            {
                message = "Failed to create product",
                description = errors[0].Description
            }));
    }

    /// <summary>
    /// Update an existing product by its ID.
    /// </summary>
    [HttpPut("{id:guid}")]
    public async Task<IActionResult> Update(Guid id, [FromBody] ProductUpdateDto updateDto, CancellationToken token)
    {
        var result = await productService.UpdateProductAsync(id, updateDto, token);

        return result.Match<IActionResult>(
            _ => NoContent(), // Trả về 204 nếu cập nhật thành công
            errors => BadRequest(new
            {
                message = "Update failed",
                description = errors[0].Description
            }));
    }

    /// <summary>
    /// Delete a product from the system.
    /// </summary>
    [HttpDelete("{id:guid}")]
    public async Task<IActionResult> Delete(Guid id, CancellationToken token)
    {
        var result = await productService.DeleteProductAsync(id, token);

        return result.Match<IActionResult>(
            _ => NoContent(),
            errors => BadRequest(new
            {
                message = "Delete failed",
                description = errors[0].Description
            }));
    }

    /// <summary>
    /// Reseach the product by id
    /// </summary>
    [HttpGet("{id:guid}")]
    public async Task<IActionResult> GetById(Guid id, CancellationToken token)
    {
        var result = await productService.GetProductByIdAsync(id, token);

        // Sử dụng Pattern Matching tương tự AuthController
        return result.Match<IActionResult>(
            product => Ok(product),
            errors => NotFound(new
            {
                message = "Product not found",
                description = errors[0].Description
            }));
    }

}