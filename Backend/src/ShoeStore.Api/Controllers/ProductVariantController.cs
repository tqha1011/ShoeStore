using Microsoft.AspNetCore.Mvc;
using Microsoft.Extensions.Options;
using ShoeStore.Application.DTOs.ProductVariantDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.Upload_Image;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Api.Controllers
{
    [ApiController]
    [Route("api/products/{productGuid}/variants")]
    public class ProductVariantController(IProductVariantService variantService, IImageService imageService) : ControllerBase
    {
        [HttpPost]
        [Consumes("multipart/form-data")]
        public async Task<IActionResult> Create(Guid productGuid, [FromForm] CreateProductVariantDto dto, 
            IFormFile? image, CancellationToken token)
        {
            // 1. Upload ảnh nếu có
            if (image != null)
            {
                using var stream = image.OpenReadStream();
                var uploadResult = await imageService.UploadImageAsync(stream, image.FileName);
                if (uploadResult.IsError)
                    return BadRequest(new { message = uploadResult.FirstError.Description });

                dto.ImageUrl = uploadResult.Value; 
            }

            // 2. Tạo variant
            var result = await variantService.CreateAsync(productGuid, dto, token);
            return result.Match<IActionResult>(
                variant => Ok(variant),
                errors => BadRequest(new { message = errors[0].Description })
            );
        }
    }
}
