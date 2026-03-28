using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface;

namespace ShoeStore.Api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ProductsController(IProductService _productService) : ControllerBase
    {
        [HttpGet("search")]
        public async Task<IActionResult> Search([FromQuery] ProductSearchRequest request, CancellationToken token)
        {
            var results = await _productService.GetProductsAsync(request, token);

            return results.Match<IActionResult>(
                    pageResult => Ok(pageResult),
                    errors => BadRequest(errors)
                );
        }
    }
}
