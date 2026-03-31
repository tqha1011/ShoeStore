using ErrorOr;

namespace ShoeStore.Application.Interface.UploadImage;

public interface IImageService
{
    Task<ErrorOr<string>> UploadImageAsync(Stream stream, string fileName);
}