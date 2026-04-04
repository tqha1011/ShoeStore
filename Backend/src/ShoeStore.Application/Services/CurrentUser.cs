using System.Security.Claims;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using Microsoft.AspNetCore.Http;

namespace ShoeStore.Application.Services
{
    public class CurrentUser : ICurrentUser
    {
        private readonly IHttpContextAccessor _accessor;

        public CurrentUser(IHttpContextAccessor accessor) => _accessor = accessor;

        private ClaimsPrincipal? User => _accessor.HttpContext?.User;

        public int? Id
        {
            get
            {
                // Nếu chưa login -> trả về null, không crash
                if (User?.Identity?.IsAuthenticated != true) return null;

                var claim = User?.FindFirst(ClaimTypes.NameIdentifier)?.Value;
                return int.TryParse(claim, out var id) ? id : null;
            }
        }

        public bool IsAdmin => User?.IsInRole("Admin") ?? false;
        public bool IsAuthenticated => User?.Identity?.IsAuthenticated ?? false;
    }
}
