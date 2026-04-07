using ErrorOr;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using ShoeStore.Application.DTOs.InvoiceDTOs;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Application.Services;
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

        [HttpPut("{invoiceGuid}/status")]
        public async Task<IActionResult> UpdateInvoiceStatus(Guid invoiceGuid, [FromBody] UpdateStateRequestDto request, CancellationToken token)
        {
            var result = await invoiceService.UpdateInvoiceStateAsync(invoiceGuid, request, token);
            return result.Match<IActionResult>(
               success => Ok(new
               {
                   message = "Invoice status updated successfully",
                   newStatus = request.Status
               }),
                errors => errors[0].Code switch
                {
                    "Invoice.NotFound" => NotFound(new
                    {
                        message = "Invoice not found",
                        description = errors[0].Description
                    }),
                    "Invoice.Unauthorized" => Unauthorized(new
                    {
                        message = "You are not authorized to update this invoice",
                        description = errors[0].Description
                    }),
                    "Invoice.Forbidden" => Forbid(),
                    "Invoice.Validation" => BadRequest(new
                    {
                        message = "Validation failed",
                        description = errors[0].Description
                    }),
                    // Lỗi mặc định
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
