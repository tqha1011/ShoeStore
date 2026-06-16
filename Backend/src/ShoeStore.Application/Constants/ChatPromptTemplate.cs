using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Constants;

public static class ChatPromptTemplate
{
    public static string GenerateProductRagUserMessage(string userMessage, string inventoryContext)
    {
        return $"""
                [CURRENT USER MESSAGE]
                {userMessage}

                [CURRENT INVENTORY CONTEXT]
                {inventoryContext}

                [ANSWER INSTRUCTIONS]
                The current inventory context is the only source of truth for product names, availability, colors, sizes, and prices.
                It overrides prior conversation when there is any conflict.
                Answer the current user message using the inventory context contract from the system prompt.
                """;
    }

    public static string GenerateInvoiceReportUserMessage(string userMessage, InvoiceReportQueryDto query,
        InvoiceResultDto invoiceResult)
    {
        var invoiceRows = invoiceResult.Result.Count == 0
            ? "No invoices found."
            : string.Join(Environment.NewLine,
                invoiceResult.Result.Select(invoice =>
                    $"- Code: {invoice.InvoiceCode}, Status: {invoice.Status}, Final price: {invoice.Price} VND"));

        return $"""
                [CURRENT USER MESSAGE]
                {userMessage}

                [INVOICE QUERY]
                Status: {query.Status}
                DayOffset: {query.DayOffset?.ToString() ?? "null"}
                ExactDate: {query.ExactDate?.ToString("yyyy-MM-dd") ?? "null"}

                [INVOICE DATA]
                TotalInvoice: {invoiceResult.TotalInvoice}
                TotalRevenue: {invoiceResult.TotalRevenue} VND
                Invoices:
                {invoiceRows}

                [ANSWER INSTRUCTIONS]
                Answer using only the invoice data above. Do not invent orders, revenue, dates, or statuses.
                Keep the response concise for the store admin.
                """;
    }
}
