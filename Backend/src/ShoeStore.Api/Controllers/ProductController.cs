using ErrorOr;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface;

namespace ShoeStore.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize(Roles = "User")]
    public class ProductsController(IProductService productService) : ControllerBase
    {
        [HttpGet("search")]
        public async Task<IActionResult> Search([FromQuery] ProductSearchRequest request, CancellationToken token)
        {
            var results = await productService.GetProductsAsync(request, token);

            return results.Match<IActionResult>(
                    pageResult => Ok(pageResult),
                    errors => BadRequest(errors)
                );
        }
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
                errors => errors.Any(e => e.Type == ErrorType.NotFound)
                    ? NotFound(new
                    {
                        message = "Product not found",
                        description = errors[0].Description
                    })
                    : BadRequest(new
                    {
                        message = "Failed to get product detail",
                        description = errors[0].Description
                    })
            );

            return response;
        }

    }
}
