using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.Interface;
using Microsoft.AspNetCore.Authorization;

namespace ShoeStore.API.Controllers;
[Route("api/admin/products")]
[ApiController]
[Authorize(Roles = "Admin")] 
public class AdminProductController(IProductService productService) : ControllerBase
{
    
}