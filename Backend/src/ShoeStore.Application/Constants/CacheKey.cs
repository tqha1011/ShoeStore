using System.Security.Cryptography;
using System.Text;
using System.Text.Json;
using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.Constants;

public static class CacheKey
{
    public static string GenerateProductListCacheKey(ProductSearchRequest request)
    {
        var baseKey = $"shoestore:product:p{request.PageIndex}:s{request.PageSize}";

        var hasFilter = !string.IsNullOrWhiteSpace(request.Keyword) ||
                        !string.IsNullOrWhiteSpace(request.Brand) ||
                        request.ProductId.HasValue ||
                        request.MinPrice.HasValue ||
                        request.MaxPrice.HasValue ||
                        (request.ListColorId != null && request.ListColorId.Count != 0) ||
                        (request.ListSizeId != null && request.ListSizeId.Count != 0) ||
                        request.Sort != "default";

        if (!hasFilter) return baseKey;

        var sortedColors = request.ListColorId?.OrderBy(id => id).ToList();
        var sortedSizes = request.ListSizeId?.OrderBy(id => id).ToList();

        var filterData = new
        {
            q = request.Keyword?.Trim().ToLower(),
            b = request.Brand?.Trim().ToLower(),
            pId = request.PageIndex,
            maxP = request.MaxPrice,
            minP = request.MinPrice,
            c = sortedColors,
            s = sortedSizes,
            sort = request.Sort?.Trim().ToLower()
        };

        var jsonString = JsonSerializer.Serialize(filterData);
        var filterHash = GenerateMd5Hash(jsonString);
        return $"{baseKey}:f_{filterHash}";
    }

    public static string GenerateProductDetailsCacheKey(Guid productGuid)
    {
        return $"shoestore:product:details:{productGuid}";
    }

    private static string GenerateMd5Hash(string input)
    {
        var inputBytes = Encoding.UTF8.GetBytes(input);
        var hashBytes = MD5.HashData(inputBytes);
        return Convert.ToHexString(hashBytes).ToLower();
    }
}