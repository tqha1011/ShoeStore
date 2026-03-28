using System;
using System.Collections.Generic;
using System.Text;
using ErrorOr;
namespace ShoeStore.Application.Interface.Upload_Image
{
    public interface IImageService
    {
        Task<ErrorOr<string>> UploadImageAsync(Stream stream, string fileName);
    }
}
