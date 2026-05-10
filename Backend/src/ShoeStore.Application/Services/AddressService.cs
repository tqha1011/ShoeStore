using ErrorOr;
using ShoeStore.Application.DTOs.AddressDTOs;
using ShoeStore.Application.Interface.AddressInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;
using Microsoft.EntityFrameworkCore;

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

        public async Task<ErrorOr<Created>> CreateAddressAsync(Guid userGuid, CreateAddressDto createAddress, CancellationToken token)
        {
            var user = await _userRepository.GetUserByPublicIdAsync(userGuid, token);

            if (user == null)
                return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

            var address = new UserAddress
            {
                UserId = user.Id,
                Address = $"{createAddress.DetailAddress}, {createAddress.District}, {createAddress.Province}",
                CreatedAt = DateTime.UtcNow
            };

            _addressRepository.Add(address);
            await _uow.SaveChangesAsync(token);
            return Result.Created;
        }

        public async Task<ErrorOr<Deleted>> DeleteAddressAsync(Guid userGuid, int addressId, CancellationToken token)
        {
            if (addressId <= 0)
                return Error.Validation("Address.InvalidId", "Address id must be greater than 0.");

            var user = await _userRepository.GetUserByPublicIdAsync(userGuid, token);

            if (user == null)
                return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

            var address = await _addressRepository.GetByIdAsync(addressId, token);

            if (address == null)
                return Error.NotFound("UserAddress.NotFound", $"Address with id '{addressId}' was not found");

            _addressRepository.Delete(address);
            await _uow.SaveChangesAsync(token);
            return Result.Deleted;
        }

        public async Task<ErrorOr<AddressResponseDto>> GetAddressbyIdAsync(Guid userGuid, int addressId, CancellationToken token)
        {
            if (addressId <= 0)
                return Error.Validation("Address.InvalidId", "Address id must be greater than 0.");

            var user = await _userRepository.GetUserByPublicIdAsync(userGuid, token);

            if (user == null)
                return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

            var address = await _addressRepository.GetByIdAsync(addressId, token);

            if (address == null)
                return Error.NotFound("UserAddress.NotFound", $"Address with id '{addressId}' was not found");

            return new AddressResponseDto { Address = address.Address};
        }

        public async Task<ErrorOr<IEnumerable<AddressResponseDto>>> GetAllAddressAsync(Guid userGuid, CancellationToken token)
        {
            var user = await _userRepository.GetUserByPublicIdAsync(userGuid, token);

            if (user == null)
                return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

            var addresses = await _addressRepository.GetAll(userGuid).Select(v => new AddressResponseDto
            {
                Address = v.Address
            }).ToListAsync(token);

            if (!addresses.Any())
                return Error.NotFound("Address.NotFound", "No addresses were found for this user.");

            return addresses;
        }

        public async Task<ErrorOr<Updated>> UpdateAddressAsync(Guid userGuid, int addressId, UpdateAddressDto updateAddress, CancellationToken token)
        {
            if (addressId <= 0)
                return Error.Validation("Address.InvalidId", "Address id must be greater than 0.");

            var user = await _userRepository.GetUserByPublicIdAsync(userGuid, token);

            if(user == null)
                return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

            var address = await _addressRepository.GetByIdAsync(addressId, token);

            if (address == null)
                return Error.NotFound("UserAddress.NotFound", $"Address with id '{addressId}' was not found");

            address.Address = $"{updateAddress.DetailAddress}, {updateAddress.District}, {updateAddress.Province}" ?? address.Address;
            _addressRepository.Update(address);
            await _uow.SaveChangesAsync(token);
            return Result.Updated;
        }
    }
}
