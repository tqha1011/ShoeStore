using Asp.Versioning;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.RateLimiting;
using ShoeStore.Application.DTOs.AddressDTOs;
using ShoeStore.Application.Interface.AddressInterface;

namespace ShoeStore.Api.Controllers;

/// <summary>
///     Controller for managing user addresses.
///     Provides endpoints for creating, updating, deleting, and retrieving addresses.
///     All endpoints require user authorization and are rate-limited.
/// </summary>
/// <param name="addressService">Service for handling address operations.</param>
[ApiController]
[Route("api/address")]
[ApiVersion(1)]
[Authorize]
[EnableRateLimiting("limit-per-user")]
public class AddressController(IAddressService addressService) : ControllerBase
{
    /// <summary>
    ///     Creates a new address for a specific user.
    /// </summary>
    /// <param name="userGuid">The unique identifier (GUID) of the user.</param>
    /// <param name="dto">The address details to be created.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="201">Address created successfully.</response>
    /// <response code="404">User not found.</response>
    /// <response code="400">Bad request; invalid address details.</response>
    /// <returns>An action result indicating the outcome of the creation process.</returns>
    [HttpPost("{userGuid}")]
    [ProducesResponseType(StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> CreateAddress(Guid userGuid, [FromBody] CreateAddressDto dto,
        CancellationToken token)
    {
        var result = await addressService.CreateAddressAsync(userGuid, dto, token);

        return result.Match<IActionResult>(
            _ => StatusCode(StatusCodes.Status201Created, new { message = "Address created successfully" }),
            errors => errors[0].Code switch
            {
                "User.NotFound" => NotFound(new { message = errors[0].Description }),
                _ => BadRequest(new { message = "Failed to create address", detail = errors[0].Description })
            }
        );
    }

    /// <summary>
    ///     Updates an existing address for a specific user.
    /// </summary>
    /// <param name="userGuid">The unique identifier (GUID) of the user.</param>
    /// <param name="addressId">The identifier of the address to update.</param>
    /// <param name="dto">The updated address details.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Address updated successfully.</response>
    /// <response code="404">User or address not found.</response>
    /// <response code="400">Bad request; invalid update details.</response>
    /// <returns>An action result indicating the outcome of the update process.</returns>
    [HttpPut("{userGuid}/{addressId}")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> UpdateAddress(Guid userGuid, int addressId, [FromBody] UpdateAddressDto dto,
        CancellationToken token)
    {
        var result = await addressService.UpdateAddressAsync(userGuid, addressId, dto, token);

        return result.Match<IActionResult>(
            _ => Ok(new { message = "Address updated successfully" }),
            errors => errors[0].Code switch
            {
                "User.NotFound" or "UserAddress.NotFound" => NotFound(new { message = errors[0].Description }),
                "Address.InvalidId" => BadRequest(new { message = errors[0].Description }),
                _ => BadRequest(new { message = "Failed to update address", detail = errors[0].Description })
            }
        );
    }

    /// <summary>
    ///     Deletes an existing address for a specific user.
    /// </summary>
    /// <param name="userGuid">The unique identifier (GUID) of the user.</param>
    /// <param name="addressId">The identifier of the address to delete.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Address deleted successfully.</response>
    /// <response code="403">Forbidden; user does not have permission to delete this address.</response>
    /// <response code="404">User or address not found.</response>
    /// <returns>An action result indicating the outcome of the deletion process.</returns>
    [HttpDelete("{userGuid}/{addressId}")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status403Forbidden)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> DeleteAddress(Guid userGuid, int addressId, CancellationToken token)
    {
        var result = await addressService.DeleteAddressAsync(userGuid, addressId, token);

        return result.Match<IActionResult>(
            _ => Ok(new { message = "Address deleted successfully" }),
            errors => errors[0].Code switch
            {
                "User.NotFound" or "UserAddress.NotFound" => NotFound(new { message = errors[0].Description }),
                "Address.Forbidden" => Forbid(),
                _ => BadRequest(new { message = "Failed to delete address", detail = errors[0].Description })
            }
        );
    }

    /// <summary>
    ///     Retrieves a specific address by its identifier for a specific user.
    /// </summary>
    /// <param name="userGuid">The unique identifier (GUID) of the user.</param>
    /// <param name="addressId">The identifier of the address to retrieve.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Address retrieved successfully.</response>
    /// <response code="404">User or address not found.</response>
    /// <returns>An action result containing the address details if found.</returns>
    [HttpGet("{userGuid}/{addressId}")]
    [ProducesResponseType(typeof(AddressResponseDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetAddressId(Guid userGuid, int addressId, CancellationToken token)
    {
        var result = await addressService.GetAddressbyIdAsync(userGuid, addressId, token);

        return result.Match<IActionResult>(
            address => Ok(address),
            errors => errors[0].Code switch
            {
                "User.NotFound" or "UserAddress.NotFound" => NotFound(new { message = errors[0].Description }),
                _ => BadRequest(new { message = "Failed to retrieve address", detail = errors[0].Description })
            }
        );
    }

    /// <summary>
    ///     Retrieves all addresses associated with a specific user.
    /// </summary>
    /// <param name="userGuid">The unique identifier (GUID) of the user.</param>
    /// <param name="token">Cancellation token for the request.</param>
    /// <response code="200">Addresses retrieved successfully.</response>
    /// <response code="404">User not found or no addresses found.</response>
    /// <returns>An action result containing a list of addresses for the user.</returns>
    [HttpGet("all/{userGuid}")]
    [ProducesResponseType(typeof(IEnumerable<AddressResponseDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetAllAddressForUser(Guid userGuid, CancellationToken token)
    {
        var result = await addressService.GetAllAddressAsync(userGuid, token);

        return result.Match<IActionResult>(
            addresses => Ok(addresses),
            errors => errors[0].Code switch
            {
                "User.NotFound" or "Address.NotFound" => NotFound(new { message = errors[0].Description }),
                _ => BadRequest(new { message = "Failed to retrieve addresses", detail = errors[0].Description })
            }
        );
    }
}