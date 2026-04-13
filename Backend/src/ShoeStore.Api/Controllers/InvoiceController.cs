using ErrorOr;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Services;
namespace ShoeStore.Api.Controllers
{
    [ApiController]
    [Route("api/invoice")]
    [Authorize]
    public class InvoiceController(IInvoiceService invoiceService) : ControllerBase
    {
        [HttpGet("get-all")]
        public async Task<IActionResult> GetInvoice([FromQuery] InvoiceRequestDto request, CancellationToken token)
        {
            var result = await invoiceService.GetInvoiceAsync(request, token);

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
                    "Invoice.Forbidden" => StatusCode(StatusCodes.Status403Forbidden, new
                    {
                        code = "Invoice.Forbidden",
                        message = "You do not have permission to access this invoice.",
                        description = errors[0].Description
                    }),

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
        [Authorize]
        [HttpGet("{invoiceGuid}/details")]
        public async Task<IActionResult> GetDetails(Guid invoiceGuid, CancellationToken token)
        {
            var result = await invoiceService.GetInvoiceDetailAsync(invoiceGuid, token);

            return result.Match<IActionResult>(
                details => Ok(new
                {
                    message = "Get invoice details successfully",
                    data = details
                }),
                errors => errors[0].Code switch
                {
                    // Trường hợp không tìm thấy chi tiết hóa đơn
                    "InvoiceDetail.NotFound" => NotFound(new
                    {
                        message = "Invoice details not found",
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

        [HttpPut("{invoiceGuid}/admin/status")]
        [Authorize(Roles = "Admin")]
        public async Task<IActionResult> UpdateStatus(Guid invoiceGuid, [FromBody] UpdateStateRequestDto request, CancellationToken token)
        {
            var result = await invoiceService.UpdateInvoiceStateByAdminAsync(invoiceGuid, request, token);
            return result.Match<IActionResult>(
                _ => Ok(new
                {
                    message = "Update invoice status successfully"
                }),
                errors => errors[0].Code switch
                {
                    // Trường hợp không tìm thấy hóa đơn
                    "Invoice.NotFound" => NotFound(new
                    {
                        message = "Invoice not found",
                        description = errors[0].Description
                    }),
                    // Trường hợp trạng thái mới không hợp lệ
                    "Invoice.InvalidStatus" => BadRequest(new
                    {
                        message = "Invalid invoice status",
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
        [HttpPut("{invoiceGuid}/customer/status")]
        [Authorize(Roles = "Customer")]
        public async Task<IActionResult> UpdateStatusByCustomer(Guid invoiceGuid, [FromBody] UpdateStateRequestDto request, CancellationToken token)
        {
            var result = await invoiceService.UpdateInvoiceStateByUserAsync(invoiceGuid, request, token);
            return result.Match<IActionResult>(
                _ => Ok(new
                {
                    message = "Update invoice status successfully"
                }),
                errors => errors[0].Code switch
                {
                    // Trường hợp không tìm thấy hóa đơn
                    "Invoice.NotFound" => NotFound(new
                    {
                        message = "Invoice not found",
                        description = errors[0].Description
                    }),
                    // Trường hợp trạng thái mới không hợp lệ
                    "Invoice.InvalidStatus" => BadRequest(new
                    {
                        message = "Invalid invoice status",
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
