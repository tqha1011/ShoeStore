using System.Text.Json.Serialization;

namespace ShoeStore.Application.DTOs.AddressDTOs;

public sealed record WardApiResponseDto(
    [property: JsonPropertyName("code")] int Code,
    [property: JsonPropertyName("name")] string Name,
    [property: JsonPropertyName("province_code")] int ProvinceCode);
