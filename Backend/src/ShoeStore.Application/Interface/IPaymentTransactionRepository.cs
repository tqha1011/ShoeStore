using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface;

public interface IPaymentTransactionRepository : IGenericRepository<PaymentTransaction, int>
{
    Task<List<PaymentTransaction>> GetPaymentTransactionsByCodeAsync(string orderCode, CancellationToken token);

    Task<bool> CheckPaymentTransactionExistsAsync(string orderCode, decimal finalPrice, CancellationToken token);
}