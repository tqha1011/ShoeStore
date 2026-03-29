using CloudinaryDotNet;
using CloudinaryDotNet.Actions;
using ErrorOr;
using Microsoft.Extensions.Options;
using ShoeStore.Application.Interface.Upload_Image;
using ShoeStore.Infrastructure.Cloundinary;
using CloudinaryClient = CloudinaryDotNet.Cloudinary;
using Error = ErrorOr.Error;


namespace ShoeStore.Application.Services
{
    public class CloudinaryService : IImageService
    {
        private readonly CloudinaryClient _cloudinary;
        private readonly CloudinarySettings _settings;

        public CloudinaryService(IOptions<CloudinarySettings> config)
        {
            _settings = config.Value;

            var account = new Account(
                _settings.CloudName,
                _settings.ApiKey,
                _settings.ApiSecret
            );

            _cloudinary = new CloudinaryClient(account);
        }
        public async Task<ErrorOr<string>> UploadImageAsync(Stream stream, string fileName)
        {

            var uploadParams = new ImageUploadParams
            {
                File = new FileDescription(fileName, stream),
                UploadPreset = _settings.UploadPreset
            };

            // Gọi hàm Upload
            var result = await _cloudinary.UploadAsync(uploadParams);

            if (result.Error != null)
            {
                // In ra lỗi thực sự từ Cloudinary để debug
                return Error.Failure(description: result.Error.Message);
            }

            return result.SecureUrl.ToString();
        }
    }
}
