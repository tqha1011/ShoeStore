using ShoeStore.Application.DTOs.CheckOutDTOs;

namespace ShoeStore.Application.Interface;

public interface IPaymentService
{
    Task<bool> ProcessSepayWebhookAsync(SepayWebhookDto sepayWebhookDto, CancellationToken token);
}