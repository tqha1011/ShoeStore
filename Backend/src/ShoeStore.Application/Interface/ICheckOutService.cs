using ErrorOr;
using ShoeStore.Application.DTOs;

namespace ShoeStore.Application.Interface;

public interface ICheckOutService
{
    Task<ErrorOr<CheckOutResponseDto>> CheckOut(List<CheckOutRequestDto> checkOutList, CancellationToken token);
}