using ErrorOr;
using ShoeStore.Application.DTOs.AddressDTOs;
using ShoeStore.Application.Interface.AddressInterface;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

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
    }
}
