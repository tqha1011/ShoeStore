using ErrorOr;
using ShoeStore.Application.DTOs;

namespace ShoeStore.Application.Interface;

public interface ICheckOutService
{
    Task<ErrorOr<CheckOutResponseDto>> PrepareCheckOutAsync(List<CheckOutRequestDto> checkOutList,
        CancellationToken token);

    Task<ErrorOr<Created>> PlaceOrderAsync(PlaceOrderRequestDto placeOrderRequestDto, Guid publicUserId,
        CancellationToken token);
}