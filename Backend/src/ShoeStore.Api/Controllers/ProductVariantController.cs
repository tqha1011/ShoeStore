using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Application.Interface.ProductInterface;
using ShoeStore.Application.Interface.UploadImage;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for managing product variants (Admin only).
///     Provides endpoints for creating product variants with optional image uploads.
///     All operations require Admin role authorization.
/// </summary>
/// <param name="variantService">Service for handling product variant operations.</param>
/// <param name="imageService">Service for handling image uploads to cloud storage.</param>
[ApiController]
[Route("api/products/{productGuid}/variants")]
public class ProductVariantController(IProductVariantService variantService, IImageService imageService)
    : ControllerBase
{
    /// <summary>
    ///     Creates a new product variant for an existing product.
    /// </summary>
    /// <remarks>
    ///     Requires Admin role authorization and multipart/form-data with:
    ///     - <c>productGuid</c>: the parent product identifier (from URL path)
    ///     - <c>dto</c>: product variant details including size, color, price, and stock information
    ///     - <c>image</c>: (optional) product variant image file to be uploaded to Cloudinary
    ///     If an image is provided, it will be uploaded to cloud storage and the URL will be saved.
    ///     The product variant will inherit category and basic product information from the parent product.
    /// </remarks>
    /// <param name="productGuid">The unique identifier of the parent product.</param>
    /// <param name="dto">Product variant details including size, color, price, and stock information.</param>
    /// <param name="image">Optional image file for the product variant (JPG, PNG, etc.).</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">
    ///     Product variant created successfully. Returns the created variant details with image URL if
    ///     provided.
    /// </response>
    /// <response code="400">Bad request; invalid variant data or image upload failed.</response>
    /// <response code="404">Not found; the parent product does not exist.</response>
    /// <response code="401">Unauthorized; user must have Admin role authorization.</response>
    /// <response code="500">Internal server error; an unexpected server error occurred.</response>
    /// <returns>
    ///     An action result containing the created product variant details on success, or an error response describing
    ///     what went wrong.
    /// </returns>
    [HttpPost]
    [Consumes("multipart/form-data")]
    [Authorize(Roles = "Admin")]
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