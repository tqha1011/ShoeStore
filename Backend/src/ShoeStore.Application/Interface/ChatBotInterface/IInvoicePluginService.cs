using ShoeStore.Application.DTOs.ChatBotDTOs;

namespace ShoeStore.Application.Interface.ChatBotInterface;

public interface IInvoicePluginService
{
    Task<InvoiceResultDto> GetInvoiceDataAsync(string status, int dayOffset, DateTime? exactDate,
        CancellationToken token = default);
}