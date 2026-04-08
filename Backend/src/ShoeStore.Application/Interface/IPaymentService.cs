using ShoeStore.Application.DTOs;

namespace ShoeStore.Application.Interface;

public interface IPaymentService
{
    Task<bool> ProcessSepayWebhookAsync(SepayWebhookDto sepayWebhookDto, CancellationToken token);
}