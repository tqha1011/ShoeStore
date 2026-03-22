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
        public async Task<IActionResult> Search([FromQuery] ProductSearchRequest request)
        {
            var results = await productService.GetProductAsync(request);

            if(results == null || !results.Any())
            {
                return NotFound(new { message = "Sorry we could not find that product" });
            }
            return Ok(results);
        }
    }
}
