using ErrorOr;
using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.DTOs.AddressDTOs;
using ShoeStore.Application.Interface.AddressInterface;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Application.Interface.UserInterface;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Services;

public class AddressService(
    IUserRepository userRepository,
    IAddressRepository addressRepository,
    IProvinceApiService provinceApiService,
    IUnitOfWork uow) : IAddressService
{
    public async Task<ErrorOr<Created>> CreateAddressAsync(Guid userGuid, CreateAddressDto dto,
        CancellationToken token)
    {
        var userId = await userRepository.GetUserIdByPublicIdAsync(userGuid, token);
        if (userId is null)
            return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

        var locationResult = await ValidateLocationAsync(dto.ProvinceId, dto.WardId, token);
        if (locationResult.IsError)
            return locationResult.Errors;

        var (provinceName, wardName) = locationResult.Value;

        if (dto.IsDefault)
            await UnsetCurrentDefaultAsync(userId.Value, token);

        addressRepository.Add(new UserAddress
        {
            UserId = userId.Value,
            Address = BuildAddressString(dto.DetailAddress, wardName, provinceName),
            CreatedAt = DateTime.UtcNow,
            IsDefault = dto.IsDefault
        });

        await uow.SaveChangesAsync(token);
        return Result.Created;
    }

    public async Task<ErrorOr<Updated>> UpdateAddressAsync(Guid userGuid, int addressId,
        UpdateAddressDto dto, CancellationToken token)
    {
        if (addressId <= 0)
            return Error.Validation("Address.InvalidId", "Address id must be greater than 0.");

        var userId = await userRepository.GetUserIdByPublicIdAsync(userGuid, token);
        if (userId is null)
            return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

        var address = await addressRepository.GetByIdAsync(addressId, token);
        if (address is null)
            return Error.NotFound("UserAddress.NotFound", $"Address with id '{addressId}' was not found.");

        if (address.UserId != userId.Value)
            return Error.Forbidden("Address.Forbidden", "You do not have permission to update this address.");

        var locationResult = await ValidateLocationAsync(dto.ProvinceId, dto.WardId, token);
        if (locationResult.IsError)
            return locationResult.Errors;

        var (provinceName, wardName) = locationResult.Value;

        if (dto.IsDefault && !address.IsDefault)
            await UnsetCurrentDefaultAsync(userId.Value, token);

        address.Address = BuildAddressString(dto.DetailAddress, wardName, provinceName);
        address.IsDefault = dto.IsDefault;

        addressRepository.Update(address);
        await uow.SaveChangesAsync(token);
        return Result.Updated;
    }

    public async Task<ErrorOr<Deleted>> DeleteAddressAsync(Guid userGuid, int addressId, CancellationToken token)
    {
        if (addressId <= 0)
            return Error.Validation("Address.InvalidId", "Address id must be greater than 0.");

        var userId = await userRepository.GetUserIdByPublicIdAsync(userGuid, token);
        if (userId is null)
            return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

        var address = await addressRepository.GetByIdAsync(addressId, token);
        if (address is null)
            return Error.NotFound("UserAddress.NotFound", $"Address with id '{addressId}' was not found.");

        if (address.UserId != userId.Value)
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

        var userId = await userRepository.GetUserIdByPublicIdAsync(userGuid, token);
        if (userId is null)
            return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

        var address = await addressRepository.GetByIdAsync(addressId, token);
        if (address is null)
            return Error.NotFound("UserAddress.NotFound", $"Address with id '{addressId}' was not found.");

        if (address.UserId != userId.Value)
            return Error.Forbidden("Address.Forbidden", "You do not have permission to get this address.");

        return MapToResponse(address);
    }

    public async Task<ErrorOr<IEnumerable<AddressResponseDto>>> GetAllAddressAsync(Guid userGuid,
        CancellationToken token)
    {
        var exists = await userRepository.CheckUserExistsAsync(userGuid, token);
        if (!exists)
            return Error.NotFound("User.NotFound", $"User with id '{userGuid}' was not found.");

        var addresses = await addressRepository.GetAll(userGuid).ToListAsync(token);
        return addresses.Select(MapToResponse).ToList();
    }

    private async Task UnsetCurrentDefaultAsync(int userId, CancellationToken token)
    {
        var current = await addressRepository.GetDefaultAddressAsync(userId, token);
        if (current is null) return;
        current.IsDefault = false;
        addressRepository.Update(current);
    }

    private async Task<ErrorOr<(string ProvinceName, string WardName)>> ValidateLocationAsync(
        int provinceId, int wardId, CancellationToken token)
    {
        var (provinceValid, provinceName) = await provinceApiService.GetProvinceAsync(provinceId, token);
        if (!provinceValid)
            return Error.Validation("Address.InvalidProvince", $"Province with code '{provinceId}' does not exist.");

        var (wardValid, wardName, wardProvinceCode) = await provinceApiService.GetWardAsync(wardId, token);
        if (!wardValid)
            return Error.Validation("Address.InvalidWard", $"Ward with code '{wardId}' does not exist.");

        if (wardProvinceCode != provinceId)
            return Error.Validation("Address.WardProvinceMismatch",
                $"Ward '{wardId}' does not belong to province '{provinceId}'.");

        return (provinceName, wardName);
    }

    private static string BuildAddressString(string detailAddress, string wardName, string provinceName)
        => $"{detailAddress}, {wardName}, {provinceName}";

    private static AddressResponseDto MapToResponse(UserAddress address)
        => new() { Id = address.Id, Address = address.Address, IsDefault = address.IsDefault };
}
