using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ProductDTOs;
using ShoeStore.Application.Interface.ProductInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for user to search and get product's details
/// </summary>
/// <param name="productService"></param>
[ApiController]
[Route("api/[controller]")]
// [Authorize(Roles = "User")]
public class ProductsController(IProductService productService) : ControllerBase
{
    /// <summary>
    ///     API for searching products with pagination, filtering and sorting
    /// </summary>
    /// <param name="request"></param>
    /// <param name="token"></param>
    /// <returns></returns>
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
    ///     API for user to get product's details
    /// </summary>
    /// <param name="productGuid"></param>
    /// <param name="token"></param>
    /// <returns></returns>
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