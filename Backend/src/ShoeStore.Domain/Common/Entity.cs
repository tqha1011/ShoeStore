namespace ShoeStore.Domain.Common;

public abstract class Entity<TEntityId>(TEntityId id)
{
    public TEntityId Id { get; init; } = id;
}