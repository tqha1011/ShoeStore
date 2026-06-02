namespace ShoeStore.Application.DTOs.CheckOutDTOs;

public sealed record CheckOutRequestDto(Guid VariantId, int Quantity = 1);

public sealed record PrepareCheckOutRequestDto(List<CheckOutRequestDto> CheckOutList, List<int> VoucherIds);