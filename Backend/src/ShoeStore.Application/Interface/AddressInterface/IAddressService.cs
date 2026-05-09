using ErrorOr;
using ShoeStore.Application.DTOs.AddressDTOs;

namespace ShoeStore.Application.Interface.AddressInterface
{
    public interface IAddressService
    {
        Task<Created> CreateAddressAsync(Guid userGuid, CreateAddressDto createAddress, CancellationToken token);
    }
}
