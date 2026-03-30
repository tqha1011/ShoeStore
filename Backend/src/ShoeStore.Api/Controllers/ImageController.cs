using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.Interface.UploadImage;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     This controller is responsible for handling image upload requests.
///     It provides an endpoint for clients to upload images, which are then processed and stored by the IImageService.
///     The controller returns appropriate responses based on the success or failure of the upload operation.
/// </summary>
/// <param name="imageService"></param>
[ApiController]
[Route("api/[controller]")]
public class ImageController(IImageService imageService) : ControllerBase
{
    /// <summary>
    ///     API upload image to Cloudinary
    /// </summary>
    /// <param name="file"></param>
    /// <returns></returns>
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