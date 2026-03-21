using Microsoft.AspNetCore.Mvc;

namespace ShoeStore.Api.Controllers
{
    public class ProductAdminController : Controller
    {
        public IActionResult Index()
        {
            return View();
        }
    }
}
