using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class PaymentTransactionRepository(AppDbContext context)
    : GenericRepository<PaymentTransaction, int>(context), IPaymentTransactionRepository
{
    public async Task<List<PaymentTransaction>> GetPaymentTransactionsByCodeAsync(string orderCode,
        CancellationToken token)
    {
        return await DbSet.Where(p => p.OrderCode == orderCode).ToListAsync(token);
    }
}