using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Interface.UploadImage;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for CRUD Product Variants
/// </summary>
/// <param name="variantService"></param>
/// <param name="imageService"></param>
[ApiController]
[Route("api/products/{productGuid}/variants")]
public class ProductVariantController(IProductVariantService variantService, IImageService imageService)
    : ControllerBase
{
    /// <summary>
    ///     Create product variant for a product.
    ///     If image is provided, it will be uploaded and the URL will be saved in the database.
    /// </summary>
    /// <param name="productGuid"></param>
    /// <param name="dto"></param>
    /// <param name="image"></param>
    /// <param name="token"></param>
    /// <returns></returns>
    [HttpPost]
    [Consumes("multipart/form-data")]
    public async Task<IActionResult> Create(Guid productGuid, [FromForm] CreateProductVariantDto dto,
        IFormFile? image, CancellationToken token)
    {
        // 1. Upload picture if it is provided
        if (image != null)
        {
            using var stream = image.OpenReadStream();
            var uploadResult = await imageService.UploadImageAsync(stream, image.FileName);
            if (uploadResult.IsError)
                return BadRequest(new { message = uploadResult.FirstError.Description });

            dto.ImageUrl = uploadResult.Value;
        }

        // 2. Create product variant
        var result = await variantService.CreateAsync(productGuid, dto, token);
        return result.Match<IActionResult>(
            variant => Ok(variant),
            errors => errors[0].Code switch
            {
                "Product.NotFound" => NotFound(new { message = errors[0].Description }),
                _ => StatusCode(StatusCodes.Status500InternalServerError, new
                {
                    message = errors[0].Description
                })
            }
        );
    }
}