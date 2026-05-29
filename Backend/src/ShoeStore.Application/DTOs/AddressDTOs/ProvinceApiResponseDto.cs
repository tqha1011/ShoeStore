using System.Text.Json.Serialization;

namespace ShoeStore.Application.DTOs.AddressDTOs;

public sealed record ProvinceApiResponseDto(
    [property: JsonPropertyName("code")] int Code,
    [property: JsonPropertyName("name")] string? Name);
