using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.AddressDTOs;
using ShoeStore.Application.Interface.AddressInterface;

namespace ShoeStore.Api.Controllers
{
    [ApiController]
    [Route("api/address")]
    //[Authorize]
    public class AddressController(IAddressService addressService) : ControllerBase
    {
        [HttpPost("{userGuid}")]
        public async Task<IActionResult> CreateAddress(Guid userGuid, [FromBody] CreateAddressDto dto, CancellationToken token)
        {
            var result = await addressService.CreateAddressAsync(userGuid, dto, token);

            return Ok(result);
        }

        [HttpPut("{userGuid}")]
        public async Task<IActionResult> UpdateAddress(Guid userGuid, [FromBody] UpdateAddressDto dto, CancellationToken token)
        {
            var result = await addressService.UpdateAddressAsync(userGuid, dto, token);

            return Ok(result);
        }
    }
}
