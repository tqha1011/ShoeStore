using ErrorOr;
using ShoeStore.Application.DTOs.AddressDTOs;

namespace ShoeStore.Application.Interface.AddressInterface
{
    public interface IAddressService
    {
        // CREATE
        Task<ErrorOr<Created>> CreateAddressAsync(Guid userGuid, CreateAddressDto createAddress, CancellationToken token);
        // UPDATE
        Task<ErrorOr<Updated>> UpdateAddressAsync(Guid userGuid, int addressId, UpdateAddressDto updateAddress, CancellationToken token); 
        // DELETE
        Task<ErrorOr<Deleted>> DeleteAddressAsync(Guid userGuid, int addressId, CancellationToken token);
        // GET
        Task<ErrorOr<string>> GetAddressbyIdAsync(Guid userGuid, int addressId, CancellationToken token);
        Task<ErrorOr<IEnumerable<string>>> GetAllAddressAsync(Guid userGuid, CancellationToken token);
    }
}
