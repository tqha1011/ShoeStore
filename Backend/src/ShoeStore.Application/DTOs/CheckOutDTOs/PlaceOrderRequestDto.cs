namespace ShoeStore.Application.DTOs.CheckOutDTOs;

public sealed record PlaceOrderRequestDto(
    List<CheckOutRequestDto> Items,
    List<int>? VoucherIds,
    string FullName,
    string Address,
    int PaymentId,
    string PhoneNumber);