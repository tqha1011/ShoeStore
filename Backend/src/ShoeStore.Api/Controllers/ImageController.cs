using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.Interface.Upload_Image;

namespace ShoeStore.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class ImageController(IImageService imageService) : ControllerBase
    {
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
}
