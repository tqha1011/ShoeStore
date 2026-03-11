namespace ShoeStore.Domain.Common;

public abstract class Entity<TEntityId>(TEntityId id)
{
    // set id in constructor and make it init only to prevent changes after creation
    public TEntityId Id { get; init; } = id;
}