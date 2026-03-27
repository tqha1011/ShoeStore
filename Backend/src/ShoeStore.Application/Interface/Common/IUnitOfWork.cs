namespace ShoeStore.Application.Interface.Common;

public interface IUnitOfWork
{
    public Task SaveChangesAsync(CancellationToken token = default);
}