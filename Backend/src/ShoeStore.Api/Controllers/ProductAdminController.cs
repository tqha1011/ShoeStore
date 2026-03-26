using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.API.Controllers;
[Route("api/admin/products")]
[ApiController]
[Authorize(Roles = "Admin")]
public class AdminProductController(IProductService productService) : ControllerBase
{
    [HttpGet]
    public async Task<IActionResult> GetAll()
    {
        var products = await productService.GetAllProductsAsync();
        return Ok(products);
    }

    [HttpGet("search")]
    public async Task<IActionResult> Search([FromQuery] ProductSearchRequest request, CancellationToken token)
    {
        var results = await productService.GetProductAsync(request, token);

        return Ok(results);
    }

    [HttpPost]
    public async Task<IActionResult> Create([FromBody] CreateProductDto productDto)
    {
        if (!ModelState.IsValid) return BadRequest(ModelState);

        var product = await productService.AddProductAsync(productDto);

        return CreatedAtAction(nameof(GetById), new { id = product.Id }, product);
    }
}