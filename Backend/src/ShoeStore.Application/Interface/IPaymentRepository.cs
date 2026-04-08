using ShoeStore.Application.Interface.Common;
using ShoeStore.Domain.Entities;

namespace ShoeStore.Application.Interface;

public interface IPaymentRepository : IGenericRepository<Payment, int>
{
    Task<List<Payment>> GetAllPaymentMethodsAsync(CancellationToken token);

    Task<int> GetPaymentIdByCode(string code, CancellationToken token);
}