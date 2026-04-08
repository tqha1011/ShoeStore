using ErrorOr;
using ShoeStore.Application.DTOs.CheckOutDTOs;

namespace ShoeStore.Application.Interface;

public interface ICheckOutService
{
    Task<ErrorOr<CheckOutResponseDto>> PrepareCheckOutAsync(List<CheckOutRequestDto> checkOutList,
        CancellationToken token);

    Task<ErrorOr<InvoiceDto>> PlaceOrderAsync(PlaceOrderRequestDto placeOrderRequestDto, Guid publicUserId,
        bool fromCart,
        CancellationToken token);
}