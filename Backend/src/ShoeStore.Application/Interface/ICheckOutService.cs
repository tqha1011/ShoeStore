using ErrorOr;
using ShoeStore.Application.DTOs;

namespace ShoeStore.Application.Interface;

public interface ICheckOutService
{
    Task<ErrorOr<Success>> CheckOut(List<CheckOutDto> checkOutList, CancellationToken token);
}