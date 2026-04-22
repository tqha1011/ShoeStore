using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.Interface.VoucherInterface;

namespace ShoeStore.Api.Controllers
{
    [ApiController]
    [Route("api/user/vouchers")]
    [Authorize(Roles = "User")]
    public class UserVoucherController(IUserVoucherService userVoucherService) : Controller
    {
        [HttpGet("user/{userGuid}")]
        public async Task<IActionResult> GetVouchersForUser(Guid userGuid, CancellationToken token)
        {
            var result = await userVoucherService.GetAllVoucherForUserAsync(userGuid, token);
            return result.Match<IActionResult>(
                vouchers => Ok(vouchers),
                errors => BadRequest(new
                {
                    message = "Failed to retrieve vouchers for user",
                    details = errors
                }));
        }
    }
}
