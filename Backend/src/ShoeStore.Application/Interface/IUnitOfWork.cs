namespace ShoeStore.Application.Interface;

public interface IUnitOfWork
{
    public Task SaveChangesAsync(CancellationToken token = default);
}