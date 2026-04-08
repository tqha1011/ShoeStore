namespace ShoeStore.Application.DTOs;

public sealed record CheckOutResponseDto(
    List<CheckOutItemDto> Items,
    CheckOutSummaryDto Summary,
    List<string>? Warnings);