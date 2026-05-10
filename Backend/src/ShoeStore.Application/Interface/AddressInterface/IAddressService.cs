using ErrorOr;
using ShoeStore.Application.DTOs.AddressDTOs;

namespace ShoeStore.Application.Interface.AddressInterface
{
    public interface IAddressService
    {
        // CREATE
        Task<ErrorOr<Created>> CreateAddressAsync(Guid userGuid, CreateAddressDto createAddress, CancellationToken token);
        // UPDATE
        Task<ErrorOr<Updated>> UpdateAddressAsync(Guid userGuid, UpdateAddressDto updateAddress, CancellationToken token); 
    }
}
