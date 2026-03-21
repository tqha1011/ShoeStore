using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.Interface;

namespace ShoeStore.Api.Controllers
{
    /// <summary>
    /// Controller for handling product related operations for customers.
    /// </summary>
    [Route("api/[controller]")]
    [ApiController]
    public class ProductsController(IProductService productService) : ControllerBase
    {
        /// <summary>
        /// Get product details by its unique identifier.
        /// </summary>
        /// <param name="id">The GUID of the product</param>
        /// <param name="token">Cancellation token</param>
        /// <returns>
        /// Product details if found, otherwise an error message.
        /// <response code="200">Returns the product details</response>
        /// <response code="404">Product not found</response>
        /// </returns>
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
}
