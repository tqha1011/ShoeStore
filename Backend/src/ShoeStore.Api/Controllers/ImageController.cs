using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.Interface.UploadImage;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for handling image upload operations.
///     Provides endpoints for uploading images to cloud storage (Cloudinary).
///     Images are processed and stored with appropriate compression and optimization.
/// </summary>
/// <param name="imageService">Service for handling image uploads and processing.</param>
[ApiController]
[Route("api/[controller]")]
public class ImageController(IImageService imageService) : ControllerBase
{
    /// <summary>
    ///     Uploads a single image file to cloud storage (Cloudinary).
    /// </summary>
    /// <remarks>
    ///     Accepts multipart/form-data with:
    ///     - <c>file</c>: image file to upload (JPG, PNG, WebP, etc.)
    ///     The image is uploaded to Cloudinary cloud storage and a URL is returned.
    ///     Frontend can use this URL to store image references in database or display images.
    ///     Supports various image formats and applies automatic compression and optimization.
    /// </remarks>
    /// <param name="file">The image file to upload (must be a valid image format).</param>
    /// <response code="200">Image uploaded successfully. Returns the image URL from cloud storage.</response>
    /// <response code="400">Bad request; invalid file format or upload failed.</response>
    /// <response code="500">Internal server error; cloud storage service unavailable.</response>
    /// <returns>
    ///     An action result containing the uploaded image URL on success, or an error response describing what went
    ///     wrong.
    /// </returns>
    [HttpPost("image")]
    //[Consumes("multipart/form-data")]
    public async Task<IActionResult> UploadImage([FromForm] IFormFile file)
    {
        using var stream = file.OpenReadStream();

        var result = await imageService.UploadImageAsync(stream, file.FileName);

        var response = result.Match<IActionResult>(
            url => Ok(new
            {
                message = "Upload success",
                imageUrl = url
            }),
            errors => BadRequest(new
            {
                message = "Upload failed",
                description = errors[0].Description
            })
        );
        return response;
    }
}