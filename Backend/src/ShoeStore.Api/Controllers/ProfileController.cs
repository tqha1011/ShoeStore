using Asp.Versioning;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.ProfileDTOs;
using ShoeStore.Application.Interface;
using ShoeStore.Application.Interface.ProfileInterface;

namespace ShoeStore.Api.Controllers;

[Authorize]
[Route("api/profile")]
[ApiVersion(1)]
[ApiController]
public class ProfileController(IProfileService profileService) : ControllerBase
{
    [HttpGet("{userGuid}")]
    public async Task<IActionResult> GetProfile(Guid userGuid, CancellationToken token)
    {
        var result = profileService.GetProfile(userGuid, token);

        return Ok(result);
    }

    [HttpPut("{userGuid}")]
    public async Task<IActionResult> UpdateProfile(Guid userGuid, UpdateProfileDto updateProfileDto, CancellationToken token)
    {
        var result = profileService.UpdateProfile(userGuid, updateProfileDto, token);
        return Ok(result);
    }
}
