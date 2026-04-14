using ShoeStore.Application.DTOs.CheckOutDTOs;

namespace ShoeStore.Application.Interface.CheckoutInterface;

public interface IPaymentService
{
    Task<bool> ProcessSepayWebhookAsync(SepayWebhookDto sepayWebhookDto, CancellationToken token);
}