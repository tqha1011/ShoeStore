using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.AddressDTOs;
using ShoeStore.Application.Interface.AddressInterface;

namespace ShoeStore.Api.Controllers
{
    [ApiController]
    [Route("api/address")]
    [Authorize]
    public class AddressController(IAddressService addressService) : ControllerBase
    {
        [HttpPost("{userGuid}")]
        public async Task<IActionResult> CreateAddress(Guid userGuid, [FromBody] CreateAddressDto dto, CancellationToken token)
        {
            var result = await addressService.CreateAddressAsync(userGuid, dto, token);

            return Ok(result);
        }

        [HttpPut("{userGuid}/{addressId}")]
        public async Task<IActionResult> UpdateAddress(Guid userGuid, int addressId, [FromBody] UpdateAddressDto dto, CancellationToken token)
        {
            var result = await addressService.UpdateAddressAsync(userGuid, addressId, dto, token);

            return Ok(result);
        }

        [HttpDelete("{userGuid}/{addressId}")]
        public async Task<IActionResult> DeleteAddress(Guid userGuid, int addressId, CancellationToken token)
        {
            var result = await addressService.DeleteAddressAsync(userGuid, addressId, token);

            return Ok(result);
        }

        [HttpGet("{userGuid}/{addressId}")]
        public async Task<IActionResult> GetAddressId(Guid userGuid, int addressId, CancellationToken token)
        {
            var result = await addressService.GetAddressbyIdAsync(userGuid, addressId, token);

            return Ok(result);
        }

        [HttpGet("all")]
        public async Task<IActionResult> GetAllAddressForUser(Guid userGuid, CancellationToken token)
        {
            var result = await addressService.GetAllAddressAsync(userGuid, token);

            return Ok(result);
        }
    }
}
