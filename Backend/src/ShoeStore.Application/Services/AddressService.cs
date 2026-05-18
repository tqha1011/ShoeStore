using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.AddressDTOs;
using ShoeStore.Application.Interface.AddressInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class AddressService(IUserRepository userRepository, IAddressRepository addressRepository, IUnitOfWork uow)
    : IAddressService
{
    public async Task<ErrorOr<Created>> CreateAddressAsync(Guid userGuid, CreateAddressDto createAddress,
        CancellationToken token)
    {
        var user = await userRepository.GetUserByPublicIdAsync(userGuid, token);

        if (user == null)
            return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

        var address = new UserAddress
        {
            UserId = user.Id,
            Address = $"{createAddress.DetailAddress}, {createAddress.District}, {createAddress.Province}",
            CreatedAt = DateTime.UtcNow,
            IsDefault = createAddress.IsDefault
        };

        addressRepository.Add(address);
        await uow.SaveChangesAsync(token);
        return Result.Created;
    }

    public async Task<ErrorOr<Deleted>> DeleteAddressAsync(Guid userGuid, int addressId, CancellationToken token)
    {
        if (addressId <= 0)
            return Error.Validation("Address.InvalidId", "Address id must be greater than 0.");

        var user = await userRepository.GetUserByPublicIdAsync(userGuid, token);

        if (user == null)
            return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

        var address = await addressRepository.GetByIdAsync(addressId, token);

        if (address == null)
            return Error.NotFound("UserAddress.NotFound", $"Address with id '{addressId}' was not found");

        if (address.UserId != user.Id)
            return Error.Forbidden("Address.Forbidden", "You do not have permission to delete this address.");

        addressRepository.Delete(address);
        await uow.SaveChangesAsync(token);
        return Result.Deleted;
    }

    public async Task<ErrorOr<AddressResponseDto>> GetAddressbyIdAsync(Guid userGuid, int addressId,
        CancellationToken token)
    {
        if (addressId <= 0)
            return Error.Validation("Address.InvalidId", "Address id must be greater than 0.");

        var user = await userRepository.GetUserByPublicIdAsync(userGuid, token);

        if (user == null)
            return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

        var address = await addressRepository.GetByIdAsync(addressId, token);

        if (address == null)
            return Error.NotFound("UserAddress.NotFound", $"Address with id '{addressId}' was not found");

        if (address.UserId != user.Id)
            return Error.Forbidden("Address.Forbidden", "You do not have permission to get this address.");

        return new AddressResponseDto { Id = address.Id, Address = address.Address };
    }

    public async Task<ErrorOr<IEnumerable<AddressResponseDto>>> GetAllAddressAsync(Guid userGuid,
        CancellationToken token)
    {
        var user = await userRepository.GetUserByPublicIdAsync(userGuid, token);

        if (user == null)
            return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

        var addresses = await addressRepository.GetAll(userGuid).Select(v => new AddressResponseDto
        {
            Id = v.Id,
            Address = v.Address
        }).ToListAsync(token);

        return addresses;
    }

    public async Task<ErrorOr<Updated>> UpdateAddressAsync(Guid userGuid, int addressId, UpdateAddressDto updateAddress,
        CancellationToken token)
    {
        if (addressId <= 0)
            return Error.Validation("Address.InvalidId", "Address id must be greater than 0.");

        var user = await userRepository.GetUserByPublicIdAsync(userGuid, token);

        if (user == null)
            return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

        var address = await addressRepository.GetByIdAsync(addressId, token);

        if (address == null)
            return Error.NotFound("UserAddress.NotFound", $"Address with id '{addressId}' was not found");

        if (address.UserId != user.Id)
            return Error.Forbidden("Address.Forbidden", "You do not have permission to update this address.");

        address.Address = $"{updateAddress.DetailAddress}, {updateAddress.District}, {updateAddress.Province}" ??
                          address.Address;
        address.IsDefault = updateAddress.IsDefault;
        addressRepository.Update(address);
        await uow.SaveChangesAsync(token);
        return Result.Updated;
    }
}