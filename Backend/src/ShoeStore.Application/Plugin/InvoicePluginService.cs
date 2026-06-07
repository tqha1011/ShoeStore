using System.ComponentModel;
using Microsoft.SemanticKernel;
using ShoeStore.Application.DTOs.ChatBotDTOs;
using ShoeStore.Application.Extensions;
using ShoeStore.Application.Interface.ChatBotInterface;
using ShoeStore.Application.Interface.InvoiceInterface;
using ShoeStore.Domain.Enum;

namespace ShoeStore.Application.Plugin;

public class InvoicePluginService(IInvoiceRepository invoiceRepository) : IInvoicePluginService
{
    [KernelFunction("get-invoice-data")]
    [Description("Retrieves invoice data, order details, and total revenue. " +
                 "ONLY call this function when the user explicitly asks about ORDERS, INVOICES. DO NOT use this function to search for products. " +
                 "After receiving the response, strictly use 'TotalInvoice' and 'TotalRevenue' to provide a concise summary to the user.")]
    public async Task<InvoiceResultDto> GetInvoiceDataAsync(
        [Description("The status of the order. MUST be exactly one of the following string values: " +
                     "'Pending' (for new/unapproved orders), 'Delivering' (for shipping orders), 'Delivered' (for successfully delivered orders), 'Paid' (for completed payments), 'Cancelled' (for cancelled orders). " +
                     "If the user does not specify a status, default to 'Paid'.")]
        string status,
        [Description("The number of days offset from today. " +
                     "Today = 0. Yesterday = -1. The day before yesterday = -2. Last week = -7. " +
                     "If the user asks for a specific date (e.g., April 15th), pass 0 here.")]
        int dayOffset,
        [Description("A specific date requested by the user. MUST be formatted as 'yyyy-MM-dd'. " +
                     "ONLY pass this value when the user mentions a SPECIFIC DATE or EXACT MONTH. " +
                     "If the user uses relative time terms (like today, yesterday), you MUST pass null here.")]
        DateTime? exactDate,
        CancellationToken token = default)
    {
        DateTime endTimeVn;
        DateTime startTimeVn;
        if (exactDate != null)
        {
            startTimeVn = exactDate.Value.Date;
            endTimeVn = startTimeVn.AddDays(1); // End of the day
        }
        else if (dayOffset == 0)
        {
            endTimeVn = DateTime.UtcNow.ToVnTime();
            startTimeVn = endTimeVn.Date; // From midnight to now
        }
        else
        {
            startTimeVn = DateTime.UtcNow.ToVnTime().Date.AddDays(dayOffset);
            endTimeVn = startTimeVn.AddDays(1);
        }

        var startTimeUtc = startTimeVn.ToUtcFromVnTime();
        var endTimeUtc = endTimeVn.ToUtcFromVnTime();
        var invoiceStatus = status switch
        {
            "Pending" => InvoiceStatus.Pending,
            "Paid" => InvoiceStatus.Paid,
            "Cancelled" => InvoiceStatus.Cancelled,
            "Delivered" => InvoiceStatus.Delivered,
            "Delivering" => InvoiceStatus.Delivering,
            _ => InvoiceStatus.Paid
        };
        var invoices =
            await invoiceRepository.GetInvoicesByStatusAndDateRangeAsync(invoiceStatus, startTimeUtc, endTimeUtc,
                token);
        var invoiceData = invoices.Select(iv => new InvoiceDataDto(
            iv.OrderCode,
            iv.Status.ToString(),
            iv.FinalPrice)).ToList();
        var totalInvoice = invoices.Count;
        var totalPrice = invoices.Sum(iv => iv.FinalPrice);
        return new InvoiceResultDto("Success", $"Find {totalInvoice}", invoiceData, totalInvoice, totalPrice);
    }
}