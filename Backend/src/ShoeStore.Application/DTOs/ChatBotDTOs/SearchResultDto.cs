using ShoeStore.Application.DTOs.ProductDTOs;

namespace ShoeStore.Application.DTOs.ChatBotDTOs;

public sealed record SearchResultDto(
    string Status,
    string Message,
    List<ProductResponseDto> Products);