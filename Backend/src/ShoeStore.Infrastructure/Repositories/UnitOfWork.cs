using System.Data;
using Microsoft.EntityFrameworkCore.Storage;
using ShoeStore.Application.Interface.Common;
using ShoeStore.Infrastructure.Data;

namespace ShoeStore.Infrastructure.Repositories;

public class UnitOfWork(AppDbContext context) : IUnitOfWork
{
    public async Task SaveChangesAsync(CancellationToken token = default)
    {
        await context.SaveChangesAsync(token);
    }

    public async Task<IDbTransaction?> BeginTransactionAsync(CancellationToken token)
    {
        // return the current transaction
        return (await context.Database.BeginTransactionAsync(token)).GetDbTransaction();
    }

    public async Task CommitTransactionAsync(CancellationToken token = default)
    {
        await context.Database.CommitTransactionAsync(token);
    }

    public Task RollbackTransactionAsync(CancellationToken token = default)
    {
        return context.Database.RollbackTransactionAsync(token);
    }
}