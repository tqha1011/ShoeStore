using System.Text.Json.Serialization;

namespace ShoeStore.Application.DTOs.AuthDTOs;

public sealed record FacebookLoginDto(
    [property: JsonPropertyName("id")] string Id,
    [property: JsonPropertyName("email")] string Email,
    [property: JsonPropertyName("name")] string Name);