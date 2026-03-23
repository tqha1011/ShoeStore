using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.Interface;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Api.Controllers
{
    [Route("api/[controller]")]
    [ApiController]
    public class ProductsController(IProductService productService) : ControllerBase
    {
        [HttpGet("search")]
        public async Task<IActionResult> Search([FromQuery] ProductSearchRequest request, CancellationToken token)
        {
            var results = await productService.GetProductAsync(request, token);

            return Ok(results);
        }
    }
}
