using Microsoft.EntityFrameworkCore;
using ShoeStore.Application.Interface;
using ShoeStore.Domain.Entities;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class PaymentRepository(AppDbContext context) : GenericRepository<Payment, int>(context), IPaymentRepository
{
    public async Task<List<Payment>> GetAllPaymentMethodsAsync(CancellationToken token)
    {
        return await DbSet.AsNoTracking().ToListAsync(token);
    }
}