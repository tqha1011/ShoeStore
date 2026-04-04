using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.Interface.InvoiceInterface;
namespace ShoeStore.Api.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    [Authorize]
    public class InvoiceController(IInvoiceService invoiceService) : ControllerBase
    {
        [HttpGet]
        public async Task<IActionResult> GetInvoice([FromQuery] InvoiceRequestDto request, CancellationToken token)
        {
            var result = await invoiceService.GetInvoiceAsync(request, User, token);

            return result.Match<IActionResult>(
                pageResult => Ok(new
                {
                    message = "Get invoices successfully",
                    data = pageResult
                }),
                errors => errors[0].Code switch
                {
                    // Trường hợp không tìm thấy hóa đơn
                    "Invoice.NotFound" => NotFound(new
                    {
                        message = "Invoice not found",
                        description = errors[0].Description
                    }),

                    // Trường hợp người dùng không có quyền xem hóa đơn này
                    "Invoice.Forbidden" => Forbid(),

                    // Các lỗi validate dữ liệu đầu vào (nếu có)
                    "Invoice.BadRequest" => BadRequest(new
                    {
                        message = "Invalid request data",
                        description = errors[0].Description
                    }),

                    // Lỗi mặc định (Internal Server Error)
                    _ => StatusCode(StatusCodes.Status500InternalServerError, new
                    {
                        message = "An unexpected error occurred. Please try again later",
                        description = errors[0].Description
                    })
                }
            );
        }
    }
}
