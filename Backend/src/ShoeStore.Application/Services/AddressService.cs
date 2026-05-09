using ErrorOr;
using ShoeStore.Application.DTOs.AddressDTOs;
using ShoeStore.Application.Interface.AddressInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.Common;

namespace ShoeStore.Application.Services
{
    public class AddressService : IAddressService
    {
        private readonly IUserRepository _userRepository;
        private readonly IAddressRepository _addressRepository;
        private readonly IUnitOfWork _uow;

        public AddressService(IUserRepository userRepository, IAddressRepository addressRepository, IUnitOfWork uow)
        {
            _userRepository = userRepository;
            _addressRepository = addressRepository;
            _uow = uow;
        }

        public async Task<Created> CreateAddressAsync(Guid userGuid, CreateAddressDto createAddress, CancellationToken token)
        {
            throw new NotImplementedException();
        }
    }
}
