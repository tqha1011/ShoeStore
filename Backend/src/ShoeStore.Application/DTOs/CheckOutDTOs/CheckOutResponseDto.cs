namespace ShoeStore.Application.DTOs.CheckOutDTOs;

public sealed record CheckOutResponseDto(
    List<CheckOutItemDto> Items,
    CheckOutSummaryDto Summary,
    List<string>? Warnings);