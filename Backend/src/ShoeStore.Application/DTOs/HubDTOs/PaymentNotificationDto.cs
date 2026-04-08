namespace ShoeStore.Application.DTOs.HubDTOs;

public sealed record PaymentNotificationDto(
    string Message,
    decimal Amount,
    string OrderCode,
    bool IsSuccess,
    DateTime CreatedAt);