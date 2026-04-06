using System.Data;

namespace ShoeStore.Application.Interface.Common;

public interface IUnitOfWork
{
    public Task SaveChangesAsync(CancellationToken token = default);

    public Task<IDbTransaction?> BeginTransactionAsync(CancellationToken token = default);
    public Task CommitTransactionAsync(CancellationToken token = default);

    public Task RollbackTransactionAsync(CancellationToken token = default);
}